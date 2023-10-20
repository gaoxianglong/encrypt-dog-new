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
import com.gxl.encryptdog.core.shell.command.HardwareCommand;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * 获取设备唯一标识命令接口实现
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/8 23:23
 */
public class HardwareCommandImpl implements HardwareCommand {
    /**
     * 命令包含的关键字
     */
    private static final String KEY = "Hardware UUID";

    /**
     * 获取物理设备UUID
     * @return
     * @throws CommandException
     */
    @Override
    public String getHardwareId() throws CommandException {
        try (var reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(COMMAND).getInputStream()))) {
            String line = null;
            while (Objects.nonNull(line = reader.readLine())) {
                line = line.trim();
                if (line.indexOf(KEY) != -1) {
                    return line.substring(line.indexOf(Constants.COLON) + 1).trim();
                }
            }
        } catch (Throwable e) {
            throw new CommandException("Unable to execute the uuid command", e);
        }
        return null;
    }
}
