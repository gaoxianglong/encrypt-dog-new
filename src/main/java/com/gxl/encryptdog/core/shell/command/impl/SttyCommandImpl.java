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

package com.gxl.encryptdog.core.shell.command.impl;

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.error.CommandException;
import com.gxl.encryptdog.core.shell.command.SttyCommand;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * stty命令实现
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 22:15
 */
public class SttyCommandImpl implements SttyCommand {
    @Override
    public int getWidth() throws CommandException {
//        return exec(1);
        return 120;
    }

    @Override
    public int getHeight() throws CommandException {
//        return exec(0);
        return 30;
    }

    /**
     * 执行stty size命令
     * @param index
     * @return
     * @throws CommandException
     */
    private int exec(int index) throws CommandException {
        var rlt = 0;
        try (var reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(COMMAND).getInputStream()))) {
            rlt = Integer.parseInt(reader.readLine().split(Constants.SPACE)[index]);
        } catch (Throwable e) {
            throw new CommandException("Unable to execute the stty size command", e);
        }
        return rlt;
    }
}
