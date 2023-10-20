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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Cached线程池，可回收缓存线程池，空闲线程允许进行回收
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/23 13:56
 */
public class CachedThreadPool implements ThreadPool<ExecutorService> {
    /**
     * 最大线程数缺省为CPU核心数/2
     */
    private static final int      DEFAULT_MAX_POOL_SIZE   = Runtime.getRuntime().availableProcessors() >> 1;
    /**
     * 核心线程数缺省为DEFAULT_MAX_POOL_SIZE
     */
    private static final int      DEFAULT_CORE_POOL_SIZE  = DEFAULT_MAX_POOL_SIZE;
    /**
     * 线程池的最大队列数缺省为32767
     */
    private static final int      DEFAULT_QUEUE_SIZE      = Short.MAX_VALUE;
    /**
     * 缺省线程池线程的存活时间为2s
     */
    private static final int      DEFAULT_KEEP_ALIVE_TIME = 2;
    /**
     * 缺省线程池名称
     */
    private static final String   DEFAULT_THREADPOOL_NAME = "dogWorkerThreadPool-%d";
    /**
     * 缺省存活时间单位
     */
    private static final TimeUnit DEFAULT_UNIT            = TimeUnit.SECONDS;

    /**
     * 创建FixedThreadPool线程池
     * @return
     */
    @Override
    public ExecutorService buildExecutor() {
        return new ThreadPoolExecutor(
            // 设置核心线程数
            DEFAULT_CORE_POOL_SIZE,
            // 设置最大线程数
            DEFAULT_MAX_POOL_SIZE,
            // 设置存活时间
            DEFAULT_KEEP_ALIVE_TIME,
            // 设置存活时间单位
            DEFAULT_UNIT,
            // 设置线程池队列数
            new LinkedBlockingQueue<>(DEFAULT_QUEUE_SIZE),
            // 设置ThreadFactory
            new ThreadFactoryBuilder().setNameFormat(DEFAULT_THREADPOOL_NAME).setDaemon(true).build(),
            // 设置拒绝策略
            new ThreadPoolExecutor.AbortPolicy());
    }
}
