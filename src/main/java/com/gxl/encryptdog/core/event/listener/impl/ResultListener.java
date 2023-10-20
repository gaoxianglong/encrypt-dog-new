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

package com.gxl.encryptdog.core.event.listener.impl;

import com.gxl.encryptdog.base.error.ListenerException;
import com.gxl.encryptdog.core.event.BaseEvent;
import com.gxl.encryptdog.core.event.ResultEvent;
import com.gxl.encryptdog.core.event.listener.BaseListenerImpl;

import java.util.Objects;

/**
 * 执行结果监听
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/19 17:51
 */
public class ResultListener extends BaseListenerImpl {
    @Override
    public void onEvent(BaseEvent event) throws ListenerException {
        try {
            var resultEvent = (ResultEvent) event;
            var encryptContext = resultEvent.getEncryptContext();
            var resultContext = encryptContext.getResultContext();

            var viewResult = resultContext.getDashboardViewResult();
            if (Objects.nonNull(resultEvent.getSuccessCount())) {
                // 递增成功文件数
                viewResult.getSuccessFileNumber().incrementAndGet();
            }
            if (Objects.nonNull(resultEvent.getFailedCount())) {
                // 递增失败文件数
                viewResult.getFailedFileNumber().incrementAndGet();
            }
            // 获取文件大小
            var fileSize = Integer.parseInt(viewResult.getFileSize());
            var successRate = (double) viewResult.getSuccessFileNumber().get() / fileSize * 100;
            var failedRate = (double) viewResult.getFailedFileNumber().get() / fileSize * 100;
            // 设置成功率
            viewResult.setSuccessRate(String.format("%.2f", successRate) + "%");
            // 设置失败率
            viewResult.setFailedRate(String.format("%.2f", failedRate) + "%");

            var key = encryptContext.getOperationVO().getSourceFilePath();
            var viewStates = resultContext.getDashboardViewStates();
            var vs = viewStates.get(key);
            // 设置任务的执行结果
            vs.setResult(resultEvent.getResult());
            // 设置目标文件大小
            vs.setTargetFileSize(resultEvent.getTargetFileSize());
        } catch (Throwable e) {
            throw new ListenerException(e);
        }
    }

    @Override
    public boolean isSupport(BaseEvent event) {
        return ResultEvent.class.isAssignableFrom(event.getClass());
    }
}
