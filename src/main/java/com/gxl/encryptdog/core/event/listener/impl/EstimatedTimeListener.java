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
import com.gxl.encryptdog.core.event.EstimatedTimeEvent;
import com.gxl.encryptdog.core.event.listener.BaseListenerImpl;

/**
 * 预计耗时监听
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/19 17:00
 */
public class EstimatedTimeListener extends BaseListenerImpl {
    @Override
    public void onEvent(BaseEvent event) throws ListenerException {
        try {
            var estimatedTimeEvent = (EstimatedTimeEvent) event;

            var encryptContext = estimatedTimeEvent.getEncryptContext();
            var operationVO = encryptContext.getOperationVO();
            var resultContext = encryptContext.getResultContext();

            var viewStates = resultContext.getDashboardViewStates();
            var vs = viewStates.get(operationVO.getSourceFilePath());
            // 设置预计耗时
            vs.setEstimatedTime(estimatedTimeEvent.getEstimatedTime());
        } catch (Throwable e) {
            throw new ListenerException(e);
        }
    }

    @Override
    public boolean isSupport(BaseEvent event) {
        return EstimatedTimeEvent.class.isAssignableFrom(event.getClass());
    }
}
