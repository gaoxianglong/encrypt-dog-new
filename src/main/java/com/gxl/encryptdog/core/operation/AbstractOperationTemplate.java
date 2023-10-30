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

/**
 * 加/解密操作模版类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 14:07
 */
@Slf4j
@Getter
public abstract class AbstractOperationTemplate implements Operation {
    /**
     * 状态机
     */
    private EncryptProcessState processState;
    /**
     * 事件广播器
     */
    private ObServerContext obServer;

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
            // 构建DecryptContext
            encryptContext = buildDecryptContext(context, sourceFileCapacity, getDefaultCapacity(sourceFileCapacity), operationVO, in, out);
            // 将任务状态流转为RUNNING
            processState.setState(encryptContext, EncryptStateEnum.RUNNING);

            // 魔术检测,如果是加密操作,则在文件起始位写入u4/32bit魔术码
            checkMagicNumber(encryptContext);

            // 最高安全性的本地化处理
            onlyLocal(encryptContext);

            // 将加/解密内容写入目标文件
            store(encryptContext);

            // 成功处理
            onSuccess(encryptContext);
        } catch (Throwable e) {
            // 加/解密操作的失败详情输出到日志目录
            log.error(String.format("File %s encryption or decryption operation failed", sourceFilePath), e);
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
    public boolean isSupport(String encryptAlgorithm, boolean isEncrypt) {
        return false;
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
     * 最高安全性的本地化处理
     *
     * @param encryptContext
     * @throws EncryptException
     * @throws IOException
     */
    private void onlyLocal(EncryptContext encryptContext) throws EncryptException, IOException {
        // 最高安全性-目标文件的文件头绑定物理设备id和fileid
        bind(encryptContext);
        // 最高安全性-创建随机秘钥文件
        createSecretkeyFile();
    }
}
