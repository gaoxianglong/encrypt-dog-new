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

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 执行结果数据上下文
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/5 13:30
 */
@Getter
public class ResultContext {
    /**
     * 执行状态数据
     */
    private Map<String, DashboardViewState>       dashboardViewStates       = new ConcurrentHashMap<>();
    /**
     * 错误详情数据
     */
    private Map<String, DashboardViewErrorDetail> dashboardViewErrorDetails = new ConcurrentHashMap<>();
    /**
     * 执行结果数据
     */
    private DashboardViewResult                   dashboardViewResult       = new DashboardViewResult();
    /**
     * 操作失败的文件个数
     */
    private AtomicInteger                         failedFileNumber          = new AtomicInteger();
    /**
     * 操作成功的文件个数
     */
    private AtomicInteger                         successFileNumber         = new AtomicInteger();
}
