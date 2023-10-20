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

package com.gxl.encryptdog.base.common.model;

import com.gxl.encryptdog.core.shell.request.ConsoleRequest;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 加/解密操作上下文信息
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 12:06
 */
@Accessors(chain = true)
@Data
public class OperationContext implements Serializable {
    @Serial
    private static final long serialVersionUID = -5334497318390904962L;
    /**
     * 控制台请求入参
     */
    private ConsoleRequest    consoleRequest;
    /**
     * 加解密操作领域模型
     */
    private List<OperationVO> operationVOList;
}
