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

package com.gxl.encryptdog.core.shell.command;

import com.gxl.encryptdog.base.error.CommandException;

/**
 * stty命令
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 22:13
 */
public interface SttyCommand {
    /**
     * stty size命令用于获取终端窗口的width和height
     */
    String[] COMMAND = { "/bin/sh", "-c", "stty size </dev/tty" };

    /**
     * 获取终端宽度
     * @return
     * @throws CommandException
     */
    int getWidth() throws CommandException;

    /**
     * 获取终端高度
     * @return
     * @throws CommandException
     */
    int getHeight() throws CommandException;
}
