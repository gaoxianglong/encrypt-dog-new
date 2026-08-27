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

package com.gxl.encryptdog.gui.infrastructure.acl;

import com.gxl.encryptdog.base.error.CommandException;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.core.shell.view.View;
import com.gxl.encryptdog.gui.application.dto.OperationListener;

/**
 * GUI视图渲染器,实现core的View接口。
 * 由ViewSchedule在调度线程每秒回调,draw内只提取数据并经回调推送到应用层,不做任何Swing操作。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class GuiDashboardView implements View {
    /**
     * 进度回调
     */
    private final OperationListener    listener;
    /**
     * 加密核心防腐层
     */
    private final EncryptCoreFacade    facade;
    /**
     * 执行结果数据上下文缓存,操作完成后由防腐层读取最终结果
     */
    private volatile ResultContext     resultContext;

    public GuiDashboardView(OperationListener listener, EncryptCoreFacade facade) {
        this.listener = listener;
        this.facade = facade;
    }

    /**
     * 执行view渲染,仅提取数据不做Swing操作
     * @param context
     * @throws CommandException
     */
    @Override
    public void draw(ResultContext context) throws CommandException {
        // 缓存执行结果数据上下文
        this.resultContext = context;
        // 提取进度快照并推送到应用层
        listener.onProgress(facade.convertProgress(context));
    }

    /**
     * GUI视图不依赖控制台环境,无终端时仍需被定时调度(负责进度刷新与闭锁释放)
     * @return
     */
    @Override
    public boolean isConsoleRequired() {
        return false;
    }

    /**
     * 获取执行结果数据上下文
     * @return
     */
    public ResultContext getResultContext() {
        return resultContext;
    }
}
