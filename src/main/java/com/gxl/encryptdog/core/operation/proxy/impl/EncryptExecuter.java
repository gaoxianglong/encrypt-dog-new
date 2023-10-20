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

package com.gxl.encryptdog.core.operation.proxy.impl;

import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.core.operation.Operation;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;

import java.util.concurrent.CountDownLatch;

/**
 * 加/解密执行器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 12:36
 */
public class EncryptExecuter implements Runnable {
    /**
     * 加/解密器
     */
    private Operation      operation;
    /**
     * 加解密操作领域模型
     */
    private OperationVO    operationVO;
    /**
     * 执行结果上下文
     */
    private ResultContext  context;
    /**
     * 闭锁
     */
    private CountDownLatch latch;

    public EncryptExecuter(Operation operation, OperationVO operationVO, ResultContext context, CountDownLatch latch) {
        this.operation = operation;
        this.operationVO = operationVO;
        this.context = context;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            // 执行加/解密操作
            operation.execute(operationVO, context);
        } finally {
            // 资源释放
            release();
        }
    }

    /**
     * 资源释放
     */
    private void release() {
        latch.countDown();
    }
}
