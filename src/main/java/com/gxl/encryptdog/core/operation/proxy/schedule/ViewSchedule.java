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

package com.gxl.encryptdog.core.operation.proxy.schedule;

import com.gxl.encryptdog.base.common.tp.impl.ScheduledThreadPool;
import com.gxl.encryptdog.base.error.CommandException;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.core.shell.view.View;
import com.gxl.encryptdog.utils.Utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 视图渲染定时器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/5 15:56
 */
public class ViewSchedule {
    /**
     * 定时执行的单位时间
     */
    private static final TimeUnit           TIME_UNIT                = TimeUnit.SECONDS;
    /**
     *  初次执行的延迟时间
     */
    private static final int                INITIAL_DELAY            = 0;
    /**
     * 每次执行的单位时间间隔
     */
    private static final int                PERIOD                   = 1;
    /**
     * 定时任务线程池
     */
    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPool().buildExecutor();

    /**
     * dashboard视图定时渲染
     * @param view
     * @param context
     * @param latch
     */
    public static void renderDashboardView(View view, ResultContext context, CountDownLatch latch) {
        // 非控制台执行时不进行渲染
        if (!Utils.isConsole()) {
            return;
        }
        // 每隔2秒重新渲染一次dashboard视图
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                if (Thread.interrupted()) {
                    return;
                }
                // 执行视图渲染
                view.draw(context);
                if (1 == latch.getCount()) {
                    latch.countDown();
                }
            } catch (CommandException e) {
                //...
            }
        }, INITIAL_DELAY, PERIOD, TIME_UNIT);
    }

    /**
     * 停止定时渲染
     */
    public static void stop() {
        scheduledExecutorService.shutdownNow();
    }
}
