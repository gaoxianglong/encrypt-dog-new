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

package com.gxl.encryptdog.core.parse;

import com.gxl.encryptdog.base.error.CapacityException;
import com.gxl.encryptdog.base.error.ParseException;
import com.gxl.encryptdog.base.error.ValidateException;
import com.gxl.encryptdog.core.shell.request.ConsoleRequest;

import java.util.List;

/**
 * 文件解析器接口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/1 09:57
 */
public interface Parser {
    /**
     * 执行文件解析操作,如果是目录则解析目录下面所有的文件
     * @param request
     * @return
     * @throws ParseException
     * @throws CapacityException
     */
    List<String> parse(ConsoleRequest request) throws ParseException, CapacityException;
}
