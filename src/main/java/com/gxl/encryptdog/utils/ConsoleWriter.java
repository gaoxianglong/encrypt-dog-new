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

package com.gxl.encryptdog.utils;

import com.gxl.encryptdog.base.common.Constants;

import java.io.PrintStream;

/**
 * 控制台输出工具类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/21 22:12
 */
public class ConsoleWriter {
    private static final PrintStream OUT = System.out;

    /**
     * 输出内容到控制台
     *
     * @param msg
     * @param isWrap
     */
    public static void write(String msg, boolean isWrap) {
        if (isWrap) {
            OUT.print(msg);
            // 换行
            wrap();
            return;
        }
        OUT.print(msg);
    }

    /**
     * 输出内容到控制台,可指定前后换行次数
     *
     * @param msg
     * @param before
     * @param after
     */
    public static void write(String msg, int before, int after) {
        // 前换行
        for (; before > 0; before--) {
            wrap();
        }
        write(msg, false);
        // 后换行
        for (; after > 0; after--) {
            wrap();
        }
    }

    /**
     * 换行操作
     */
    public static void wrap() {
        OUT.print(Constants.WRAP);
    }
}
