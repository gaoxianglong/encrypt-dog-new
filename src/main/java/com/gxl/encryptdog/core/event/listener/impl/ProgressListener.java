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
import com.gxl.encryptdog.core.event.ProgressEvent;
import com.gxl.encryptdog.core.event.listener.BaseListenerImpl;

/**
 * 任务进度计算监听
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/15 22:54
 */
public class ProgressListener extends BaseListenerImpl {
    @Override
    public void onEvent(BaseEvent event) throws ListenerException {
        try {
            var progressEvent = (ProgressEvent) event;
            var decryptContext = progressEvent.getEncryptContext();
            var resultContext = decryptContext.getResultContext();
            var operationVO = decryptContext.getOperationVO();

            // 获取目标Dashboard具体任务的key
            var key = operationVO.getSourceFilePath();
            var viewState = resultContext.getDashboardViewStates().get(key);

            // 设置当前任务的执行进度
            viewState.setProgress((progressEvent.getProgress()));
        } catch (Throwable e) {
            throw new ListenerException(e);
        }
    }

    @Override
    public boolean isSupport(BaseEvent event) {
        return ProgressEvent.class.isAssignableFrom(event.getClass());
    }
}
