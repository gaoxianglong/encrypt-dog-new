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

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
            parse(encryptContext);

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
    public void parseSalt(EncryptContext encryptContext) throws HeaderParseException {

    }

    @Override
    public void parseMagicNumber(EncryptContext encryptContext) throws MagicNumberParseException {

    }

    @Override
    public void parseHeader(EncryptContext context) throws HeaderParseException, OperationException {

    }

    @Override
    public int getDefaultCapacity(long capacity) {
        return 0;
    }

    @Override
    public void bind(EncryptContext encryptContext) throws OperationException {

    }

    @Override
    public void store(EncryptContext encryptContext) throws EncryptException {

    }

    @Override
    public void parseVector(EncryptContext encryptContext) throws HeaderParseException, OperationException {

    }

    @Override
    public ChannelEnum getChannel() {
        return null;
    }

    /**
     * 解析加密算法跟文件头的加密算法是否一致
     * 加密操作:文件头中指定其加密类型;
     * 解密操作:验证加密算法跟文件头的加密算法是否一致
     *
     * @param context
     * @throws HeaderParseException
     */
    @Override
    public void parseEncryptType(EncryptContext context) throws HeaderParseException {

    }

    /**
     * 向文件头写入头信息
     * @param encryptContext
     * @throws HeaderParseException
     */
    @Override
    public void saveFileHeader(EncryptContext encryptContext) throws HeaderParseException {
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
     * 获取PBKDF2增强的秘钥
     * @param secretKey
     * @param salt
     * @return
     * @throws OperationException
     */
    protected abstract SecretKeySpec getGenerateKey(char[] secretKey, byte[] salt) throws OperationException;

    /**
     * 获取PBKDF2增强的秘钥
     * @param secretKey         用户明文密码
     * @param salt              盐值
     * @param keyDerivation     基于PBKDF2算法使用密码派生函数
     * @param iterationCount    PBKDF2的迭代次数
     * @param keyLength         密钥长度
     * @param algorithmName     算法名称
     * @return
     * @throws OperationException
     */
    protected SecretKeySpec getGenerateKey(char[] secretKey, byte[] salt, String keyDerivation, int iterationCount, int keyLength, String algorithmName) throws OperationException {
        try {
            // 基于PBKDF2算法使用密码派生函数
            var factory = SecretKeyFactory.getInstance(keyDerivation);
            var spec = new PBEKeySpec(secretKey, salt, iterationCount, keyLength);
            // 返回秘钥key
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), algorithmName);
        } catch (Throwable e) {
            throw new OperationException(e);
        }
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
     * @throws OperationException
     */
    private void onlyLocal(EncryptContext encryptContext) throws OperationException {
        // 最高安全性-目标文件的文件头绑定物理设备id和fileid
        bind(encryptContext);

        try {
            // 最高安全性-创建随机秘钥文件
            createSecretkeyFile();
        } catch (IOException e) {
            throw new OperationException(e);
        }
    }

    /**
     * 解析文件头
     *[Magic (4B)][HeaderLength (4B)][HeaderJson (N bytes)][Payload...]
     *
     * @param context
     * @throws ParseException
     * @throws OperationException
     */
    private void parse(EncryptContext context) throws ParseException, OperationException {
        if (Objects.isNull(context)) {
            throw new ParseException("The context info is null");
        }

        // 魔术检测,如果是加密操作,则在文件起始位写入u4/32bit魔术码
        parseMagicNumber(context);

        // 解析文件头
        parseHeader(context);

        // 最高安全性操作
        onlyLocal(context);

        // 保存文件头信息
        saveFileHeader(context);
    }
}
