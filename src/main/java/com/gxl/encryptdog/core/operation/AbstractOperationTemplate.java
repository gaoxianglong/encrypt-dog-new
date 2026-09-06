/*
 *
 *  * Copyright 2019-2119 gao_xianglong@sina.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.gxl.encryptdog.core.operation;

import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.enums.EncryptStateEnum;
import com.gxl.encryptdog.base.enums.ExecResultEnum;
import com.gxl.encryptdog.base.error.*;
import com.gxl.encryptdog.base.state.EncryptProcessState;
import com.gxl.encryptdog.base.state.impl.EncryptProcessStateImpl;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.utils.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;

/**
 * 加/解密操作模版类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 14:07
 */
@Slf4j
@Getter
public abstract class AbstractOperationTemplate implements OperationStrategy {
    /**
     * 状态机
     */
    private EncryptProcessState processState;
    /**
     * 事件广播器
     */
    private ObServerContext     obServer;

    public AbstractOperationTemplate(ObServerContext obServer) {
        this.obServer = obServer;
        processState = new EncryptProcessStateImpl(obServer);
    }

    /**
     * 抽象模版方法
     *
     * @param operationVO
     * @param context
     * @return
     */
    @Override
    public void execute(OperationVO operationVO, ResultContext context) {
        var sourceFilePath = operationVO.getSourceFilePath();
        var targetFilePath = operationVO.getTargetFilePath();

        // 获取源文件容量
        var sourceFileCapacity = operationVO.getSourceFileCapacity();
        EncryptContext encryptContext = null;
        try (var in = new BufferedInputStream(new FileInputStream(sourceFilePath)); var out = new BufferedOutputStream(new FileOutputStream(targetFilePath))) {
            // 构建DecryptContext上下文信息类
            encryptContext = buildDecryptContext(
                // 执行结果数据上下文
                context,
                // 源文件容量
                sourceFileCapacity,
                // 每次加/解密的操作大小,加密时AbstractEncrypt指定为10MB,解密时由AbstractDecrypt的子类自行实现
                getDefaultCapacity(sourceFileCapacity),
                // 加/解密领域模型
                operationVO,
                // 读/写文件句柄
                in, out);

            // 将任务状态流转为RUNNING
            processState.setState(encryptContext, EncryptStateEnum.RUNNING);

            // 解析文件头信息
            parseHeader(encryptContext);

            // 将加/解密内容写入目标文件
            store(encryptContext);

            // 成功处理
            onSuccess(encryptContext);
            // 日志记录
            actionLog(null, operationVO.isEncrypt(), sourceFilePath, operationVO.getSecretKey());
        } catch (Throwable e) {
            // 加/解密操作的失败详情输出到日志目录
            actionLog(e, operationVO.isEncrypt(), sourceFilePath, operationVO.getSecretKey());
            try {
                if (encryptContext == null) {
                    // 打开源/目标文件失败(如文件在解析后消失):补建最小上下文走正常失败事件链,
                    // 避免onFailure对null上下文空指针、避免该行卡在执行中且失败计数漏算
                    encryptContext = new EncryptContext()
                        // 设置执行结果数据上下文
                        .setResultContext(context)
                        // 设置加/解密领域模型
                        .setOperationVO(operationVO)
                        // 设置每次加/解密读取的文件内容大小
                        .setDefaultCapacity(0)
                        // 设置源文件容量
                        .setSourceFileCapacity(sourceFileCapacity);
                    // 记录原始失败原因,供执行列表失败图标悬停展示
                    var viewState = context.getDashboardViewStates().get(sourceFilePath);
                    if (Objects.nonNull(viewState)) {
                        viewState.setErrorMsg(e.getMessage());
                    }
                    // 状态机不允许WAITING→FINISHED直接迁移,先迁RUNNING(该场景不下发事件)再走失败处理
                    getProcessState().setState(encryptContext, EncryptStateEnum.RUNNING);
                }
                // 失败处理
                onFailure(encryptContext);
            } catch (OperationException e1) {
                //...
            }
        }
    }

    /**
     * 成功后处理
     *
     * @param encryptContext
     * @throws ResourceException
     * @throws ListenerException
     * @throws StateException
     */
    @Override
    public void onSuccess(EncryptContext encryptContext) throws OperationException {
        // 删除源文件
        deleteSourceFile(encryptContext.getOperationVO());
        // 设置任务执行结果
        encryptContext.setResult(ExecResultEnum.SUCCESS);

        // 流转任务状态为FINISHED
        getProcessState().setState(encryptContext, EncryptStateEnum.FINISHED);
    }

    /**
     * 失败后处理
     *
     * @param encryptContext
     * @throws OperationException
     */
    @Override
    public void onFailure(EncryptContext encryptContext) throws OperationException {
        // 设置任务执行结果
        encryptContext.setResult(ExecResultEnum.FAILED);
        // 流转任务状态为FINISHED
        getProcessState().setState(encryptContext, EncryptStateEnum.FINISHED);

        // 由于目标文件已经提前创建，如果操作失败则删除目标文件
        deleteTargetFile(encryptContext.getOperationVO());
    }

    @Override
    public void checkMagicNumber(EncryptContext encryptContext) throws MagicNumberException {

    }

    @Override
    public int getDefaultCapacity(long capacity) {
        return 0;
    }

    @Override
    public void bind(EncryptContext encryptContext) throws EncryptException {

    }

    @Override
    public void store(EncryptContext encryptContext) throws EncryptException {

    }

    @Override
    public void initVector(EncryptContext encryptContext) throws OperationException {

    }

    @Override
    public ChannelEnum getChannel() {
        return null;
    }

    /**
     * 加密类型检查
     * @param encryptContext
     * @throws ValidateException
     * @throws EncryptAlgorithmException
     */
    @Override
    public void checkEncryptType(EncryptContext encryptContext) throws ValidateException, EncryptAlgorithmException {

    }

    /**
     * 计算预计耗时
     *
     * @param timeConsuming  单数据块的执行耗时
     * @param encryptContext
     * @return
     */
    protected String calculationEstimatedTime(long timeConsuming, EncryptContext encryptContext) {
        var newTimeConsuming = (double) timeConsuming / 1000;
        var num = encryptContext.getSourceFileCapacity() / encryptContext.getDefaultCapacity();
        return Utils.currentTimeFormat((long) ((num < 1 ? 1 : num) * newTimeConsuming));
    }

    /**
     * 构建DecryptContext
     *
     * @param context
     * @param sourceFileCapacity
     * @param defaultCapacity
     * @param operationVO
     * @param in
     * @param out
     * @return
     */
    private EncryptContext buildDecryptContext(ResultContext context, long sourceFileCapacity, int defaultCapacity, OperationVO operationVO, BufferedInputStream in,
                                               BufferedOutputStream out) {
        return new EncryptContext()
            // 设置执行结果数据上下文
            .setResultContext(context)
            // 设置每次加/解密读取的文件内容大小
            .setDefaultCapacity(defaultCapacity)
            // 设置加/解密领域模型
            .setOperationVO(operationVO)
            // 设置源文件容量
            .setSourceFileCapacity(sourceFileCapacity)
            // 设置读文件句柄
            .setInputStream(in)
            // 设置写文件句柄
            .setOutputStream(out);
    }

    /**
     * 删除源文件
     *
     * @param operationVO
     */
    private void deleteSourceFile(OperationVO operationVO) {
        if (!operationVO.isDelete()) {
            return;
        }
        var sourceFilePath = operationVO.getSourceFilePath();
        Utils.deleteFile(sourceFilePath);
    }

    /**
     * 删除目标文件
     *
     * @param operationVO
     */
    private void deleteTargetFile(OperationVO operationVO) {
        var targetFilePath = operationVO.getTargetFilePath();
        Utils.deleteFile(targetFilePath);
    }

    /**
     * 操作记录
     * @param e
     * @param isEncrypt
     * @param file
     * @param secretKey
     */
    private void actionLog(Throwable e, boolean isEncrypt, String file, char[] secretKey) {
        final String logPrefix_1 = String.format("File {} {} successful.{}", file);
        final String logPrefix_2 = "File {} {} failed.";
        // 操作成功记录
        if (Objects.isNull(e)) {
            // 加密操作成功后记录脱敏秘钥
            if (isEncrypt) {
                log.info(logPrefix_1, file, "encryption", String.format("Masked key:%s", Utils.getMaskChar(secretKey)));
                return;
            }
            // 解密操作记录
            log.info(logPrefix_1, file, "decryption", "");
            return;
        }

        // 操作失败记录
        // 加密操作记录
        if (isEncrypt) {
            log.error(logPrefix_2, file, "encryption", e);
            return;
        }
        // 解密操作记录
        log.error(logPrefix_2, file, "decryption", e);
    }

    /**
     * 最高安全性的本地化处理
     *
     * @param encryptContext
     * @throws EncryptException
     * @throws IOException
     */
    private void onlyLocal(EncryptContext encryptContext) throws EncryptException, IOException {
        // 最高安全性-目标文件的文件头绑定物理设备id和fileid
        bind(encryptContext);
        // 最高安全性-创建随机秘钥文件,仅加密操作执行,解密时缺失直接走「记录缺失」错误路径(D8)
        if (encryptContext.getOperationVO().isEncrypt()) {
            createSecretkeyFile();
        }
    }

    /**
     * 解析文件头
     *
     * +------------------------+---------------------------+---------------------+------------------+---------------------+------------------+------------------------+--------------+
     * | magic-number: u4/32bit | encryption-type: u1/8bit  | iv-length: u1/8bit  | iv: uN/variable  | hid-length: u1/8bit | hardware-id: uX/36bit | file-id: u8/64bit | file data... |
     * | Value: 0xDE0225CF      | (e.g., encryption type)   | (length of IV)      | (IV value)       |                     | (e.g., 36 bits)       |                   |              |
     * +------------------------+---------------------------+---------------------+------------------+---------------------+------------------+------------------------+--------------+
     *
     *
     * @param context
     * @throws ValidateException
     * @throws OperationException
     * @throws IOException
     */
    private void parseHeader(EncryptContext context) throws ValidateException, OperationException, IOException {
        if (Objects.isNull(context)) {
            throw new ValidateException("The context info is null");
        }

        // 魔术检测,如果是加密操作,则在文件起始位写入u4/32bit魔术码
        checkMagicNumber(context);

        // 加密类型检测,如果是加密操作，则在魔术后写入u1/8bit加密类型
        checkEncryptType(context);

        // IV向量操作
        initVector(context);

        // 最高安全性的本地化处理,这里跟UUID有关
        onlyLocal(context);
    }
}
