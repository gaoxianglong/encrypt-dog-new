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

import com.gxl.encryptdog.base.enums.ExecResultEnum;
import com.gxl.encryptdog.base.enums.EncryptStateEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * Dashboard的执行状态数据
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 13:08
 */
@Accessors(chain = true)
@Data
public class DashboardViewState implements Serializable {
    @Serial
    private static final long serialVersionUID = 7031518741858092907L;
    /**
     * 任务名称,即源文件的全限定名
     */
    private final String      taskName;
    /**
     * 目标文件全限定名
     */
    private String            targetFile       = "-";
    /**
     * 任务执行结果
     */
    private ExecResultEnum    result           = ExecResultEnum.NOT_STARTED;
    /**
     * 任务执行状态,初始为WAITING状态
     */
    private EncryptStateEnum  state            = EncryptStateEnum.WAITING;
    /**
     * 源文件大小
     */
    private String            sourceFileSize   = "-";
    /**
     * 目标文件大小
     */
    private String            targetFileSize   = "-";
    /**
     * 失败原因
     */
    private String            errorMsg         = "-";
    /**
     * 执行进度
     */
    private String            progress         = "-";
    /**
     * 任务预计执行时间
     */
    private String            estimatedTime    = "-";

    public DashboardViewState(String taskName) {
        this.taskName = taskName;
    }
}
