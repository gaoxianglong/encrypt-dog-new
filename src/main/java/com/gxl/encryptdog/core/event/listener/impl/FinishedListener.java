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

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.error.ListenerException;
import com.gxl.encryptdog.base.error.ResourceException;
import com.gxl.encryptdog.core.event.BaseEvent;
import com.gxl.encryptdog.core.event.FinishedEvent;
import com.gxl.encryptdog.core.event.listener.BaseListenerImpl;
import com.gxl.encryptdog.core.operation.proxy.schedule.ViewSchedule;

/**
 * 完成事件监听
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/20 15:08
 */
public class FinishedListener extends BaseListenerImpl {
    @Override
    public void onEvent(BaseEvent event) throws ListenerException {
        var finishedEvent = (FinishedEvent) event;
        var resultContext = finishedEvent.getResultContext();
        var viewResult = resultContext.getDashboardViewResult();

        // 设置总耗时
        viewResult.setTimeConsuming(finishedEvent.getTimeConsuming());

        try {
            // 结束后播放提示音
            finishedEvent.getPlayer().play(Constants.DEFAULT_AUDIOS_PATH);
        } catch (ResourceException e) {
            throw new ListenerException(e);
        }
    }

    @Override
    public boolean isSupport(BaseEvent event) {
        return FinishedEvent.class.isAssignableFrom(event.getClass());
    }
}
