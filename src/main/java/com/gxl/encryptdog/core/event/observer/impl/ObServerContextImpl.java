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

package com.gxl.encryptdog.core.event.observer.impl;

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.error.ListenerException;
import com.gxl.encryptdog.core.event.BaseEvent;
import com.gxl.encryptdog.core.event.listener.BaseListener;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 观察者接口实现
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/13 11:20
 */
public class ObServerContextImpl implements ObServerContext {
    /**
     * 监听器责任链
     */
    private List<BaseListener> listenerList = new ArrayList<>();

    public ObServerContextImpl() {
        // 初始化监听器
        initListenerList();
    }

    /**
     * 事件下发
     * @param event
     * @throws ListenerException
     */
    @Override
    public void fireEvent(BaseEvent event) throws ListenerException {
        for (var listener : listenerList) {
            // 执行事件
            listener.execute(event);
        }
    }

    /**
     * 初始化监听器
     */
    private void initListenerList() {
        if (!listenerList.isEmpty()) {
            return;
        }
        try {
            // 获取目标路径下的所有类型
            var reflections = new Reflections(Constants.DEFAULT_SCAN_LISTENER_CLS_PATH);
            // 获取目标类型的所有子类
            for (var cls : reflections.getSubTypesOf(BaseListener.class)) {
                // 排除abstract
                if (Modifier.isAbstract(cls.getModifiers()) ||
                // 排除interface
                    Modifier.isInterface(cls.getModifiers())) {
                    continue;
                }
                listenerList.add(cls.getConstructor().newInstance());
            }
        } catch (Throwable e) {
            //...
        }
    }
}
