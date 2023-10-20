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

package com.gxl.encryptdog.core.operation.proxy.params;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * Dashboard错误详情数据
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/19 15:23
 */
@Accessors(chain = true)
@Data
public class DashboardViewErrorDetail implements Serializable {
    @Serial
    private static final long serialVersionUID = -966674988262193731L;
    /**
     * 任务名称,即源文件的全限定名
     */
    private final String      taskName;
    /**
     * 错误详情信息
     */
    private String            errorDetail      = "-";

    public DashboardViewErrorDetail(String taskName) {
        this.taskName = taskName;
    }
}
