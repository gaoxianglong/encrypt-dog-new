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

package com.gxl.encryptdog.base.state.impl;

import com.gxl.encryptdog.base.enums.EncryptStateEnum;
import com.gxl.encryptdog.base.enums.EncryptStateSceneEnum;
import com.gxl.encryptdog.base.enums.ExecResultEnum;
import com.gxl.encryptdog.base.error.ListenerException;
import com.gxl.encryptdog.base.error.StateException;
import com.gxl.encryptdog.base.state.EncryptProcessState;
import com.gxl.encryptdog.core.event.BaseEvent;
import com.gxl.encryptdog.core.event.ResultEvent;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.EncryptContext;
import com.gxl.encryptdog.utils.Utils;

import java.util.Objects;

/**
 * 加/解密执行状态机实现
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 13:53
 */
public class EncryptProcessStateImpl implements EncryptProcessState {
    /**
     * 事件派发器
     */
    private ObServerContext obServer;

    public EncryptProcessStateImpl(ObServerContext obServer) {
        this.obServer = obServer;
    }

    /**
     * 加/解密执行状态流转
     * @param encryptContext
     * @param to
     * @throws StateException
     * @throws ListenerException
     */
    @Override
    public void setState(EncryptContext encryptContext, EncryptStateEnum to) throws StateException, ListenerException {
        var key = encryptContext.getOperationVO().getSourceFilePath();
        // 获取执行结果数据上下文
        var viewState = encryptContext.getResultContext().getDashboardViewStates().get(key);
        var from = viewState.getState();

        // 场景正确性验证
        var scene = EncryptStateSceneEnum.checkScene(from, to);
        if (Objects.isNull(scene)) {
            throw new StateException(String.format("State machine state transition failed,from:%s,to:%s", from.name(), to.name()));
        }
        // 设置最新的加/解密状态
        viewState.setState(to);

        // 状态事件下发
        fireEvent(encryptContext, scene);
    }

    /**
     * 处理不同状态的事件
     * @param encryptContext
     * @param scene
     * @throws ListenerException
     */
    private void fireEvent(EncryptContext encryptContext, EncryptStateSceneEnum scene) throws ListenerException {
        BaseEvent event = null;
        switch (scene) {
            // 执行加/解密场景
            case ENCRYPT_SCENE:
                // 暂无事件处理
                return;
            // 加/解密结束场景
            case ENCRYPT_FINISHED_SCENE:
                // 下发ResultEvent事件
                event = buildResultEvent(encryptContext, encryptContext.getResult());
                break;
            default:
                break;
        }
        obServer.fireEvent(event);
    }

    /**
     * 构建ResultEvent
     * @param context
     * @param resultEnum
     * @return
     */
    private ResultEvent buildResultEvent(EncryptContext context, ExecResultEnum resultEnum) {
        var event = new ResultEvent(null, System.currentTimeMillis(), context);
        if (context.getResult() == ExecResultEnum.SUCCESS) {
            event.setSuccessCount(1);
        } else {
            event.setFailedCount(1);
        }
        event.setResult(resultEnum);

        // 获取目标文件路径
        var targetFilePath = context.getOperationVO().getTargetFilePath();
        event.setTargetFileSize(Utils.capacityFormat(Utils.getFileCapacity(targetFilePath)));
        return event;
    }
}
