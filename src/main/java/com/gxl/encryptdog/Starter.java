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

package com.gxl.encryptdog;

import com.gxl.encryptdog.core.shell.EncryptDogConsole;
import picocli.CommandLine;

/**
 * 加密狗启动器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/21 22:10
 */
public class Starter {
    public static void main(String[] args) {
        System.exit(new CommandLine(new EncryptDogConsole()).execute(args));
    }
}
