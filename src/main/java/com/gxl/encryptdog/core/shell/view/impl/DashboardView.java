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

package com.gxl.encryptdog.core.shell.view.impl;

import com.gxl.encryptdog.base.error.CommandException;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.core.shell.view.View;
import com.gxl.encryptdog.core.shell.view.ViewRenderUtil;
import com.gxl.encryptdog.utils.ConsoleWriter;
import com.gxl.encryptdog.utils.Utils;

/**
 * Dashboard渲染
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/5 13:20
 */
public class DashboardView implements View {
    /**
     * 执行view渲染
     * @param context
     * @throws CommandException
     */
    @Override
    public void draw(ResultContext context) throws CommandException {
        // 获取控制台窗口的width和height
        var height = COMMAND.getHeight() - 1;
        var width = COMMAND.getWidth();

        // 执行状态高度
        var stateHeight = height * 0.9F;
        // 执行结果高度
        var resultHeight = height - stateHeight;
        ConsoleWriter.write(Utils.cls(), false);
        var view = String.format("%s%s",
            // 渲染任务执行状态数据
            ViewRenderUtil.drawTaskStateInfo(width, (int) stateHeight, context),
            // 渲染执行结果数据
            ViewRenderUtil.drawTaskResultInfo(width, (int) resultHeight, context));
        ConsoleWriter.write(view, false);
    }
}
