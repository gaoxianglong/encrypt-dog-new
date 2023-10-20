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
import com.gxl.encryptdog.base.enums.ExecResultEnum;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Serial;
import java.io.Serializable;

/**
 * 具体加/解密的上下文
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/8 14:59
 */
@Data
@Accessors(chain = true)
public class EncryptContext implements Serializable {
    @Serial
    private static final long    serialVersionUID = 5508943557910068169L;
    /**
     * 执行结果数据上下文
     */
    private ResultContext        resultContext;
    /**
     * 任务执行结果
     */
    private ExecResultEnum       result;
    /**
     * 加/解密领域模型
     */
    private OperationVO          operationVO;
    /**
     * 文件唯一id,仅限于本地加/解密使用
     */
    private Long                 fileId;
    /**
     * 每次加/解密读取的文件内容大小
     */
    private Integer              defaultCapacity;
    /**
     * 目标文件总容量
     */
    private Long                 sourceFileCapacity;
    /**
     * 单数据块的执行耗时,用于后续计算预计耗时用
     */
    private Long                 timeConsuming;
    /**
     * 读文件句柄
     */
    private BufferedInputStream  inputStream;
    /**
     * 写文件句柄
     */
    private BufferedOutputStream outputStream;
}
