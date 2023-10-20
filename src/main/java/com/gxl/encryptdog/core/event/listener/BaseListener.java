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

package com.gxl.encryptdog.core.event.listener;

import com.gxl.encryptdog.base.error.ListenerException;
import com.gxl.encryptdog.core.event.BaseEvent;

/**
 * 监听器基类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/13 11:16
 */
public interface BaseListener {
    /**
     * 相关事件处理
     * @param event
     * @throws ListenerException
     */
    void onEvent(BaseEvent event) throws ListenerException;

    /**
     * 是否支持
     * @param event
     * @return
     */
    boolean isSupport(BaseEvent event);

    /**
     * 执行事件
     * @param event
     * @throws ListenerException
     */
    void execute(BaseEvent event) throws ListenerException;
}
