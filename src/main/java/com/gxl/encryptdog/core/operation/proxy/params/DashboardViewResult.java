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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dashboard执行结果数据
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/19 15:26
 */
@Accessors(chain = true)
@Data
public class DashboardViewResult implements Serializable {
    @Serial
    private static final long serialVersionUID  = -5699035541430383464L;
    /**
     * 总文件个数
     */
    private String            fileSize;
    /**
     * 操作类型
     */
    private String            operation         = "-";
    /**
     * 操作失败的文件个数
     */
    private AtomicInteger     failedFileNumber  = new AtomicInteger();
    /**
     * 操作成功的文件个数
     */
    private AtomicInteger     successFileNumber = new AtomicInteger();
    /**
     * 成功率
     */
    private String            successRate       = "0%";
    /**
     * 失败率
     */
    private String            failedRate        = "0%";
    /**
     * 总耗时
     */
    private String            timeConsuming     = "-";
}
