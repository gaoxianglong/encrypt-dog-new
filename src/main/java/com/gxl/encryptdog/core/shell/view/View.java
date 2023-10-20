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

package com.gxl.encryptdog.core.shell.view;

import com.gxl.encryptdog.base.error.CommandException;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.core.shell.command.SttyCommand;
import com.gxl.encryptdog.core.shell.command.impl.SttyCommandImpl;

/**
 * 视图渲染接口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/2 11:39
 */
public interface View {
    /**
     * STTY命令
     */
    SttyCommand COMMAND = new SttyCommandImpl();

    /**
     * 执行view渲染
     * @param context
     * @throws CommandException
     */
    void draw(ResultContext context) throws CommandException;
}
