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

package com.gxl.encryptdog.base.common.tp.impl;

import com.gxl.encryptdog.base.common.tp.ThreadPool;
import org.apache.curator.shaded.com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 定时任务线程池实现
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/8 11:09
 */
public class ScheduledThreadPool implements ThreadPool<ScheduledExecutorService> {
    /**
     * 缺省核心线程数
     */
    private static final int    DEFAULT_CORE_POOL_SIZE  = 1;
    /**
     * 缺省线程池名称
     */
    private static final String DEFAULT_THREADPOOL_NAME = "dogScheduledThreadPool-%d";

    @Override
    public ScheduledExecutorService buildExecutor() {
        return new ScheduledThreadPoolExecutor(DEFAULT_CORE_POOL_SIZE, new ThreadFactoryBuilder().setNameFormat(DEFAULT_THREADPOOL_NAME).setDaemon(true).build());
    }
}
