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

package com.gxl.encryptdog.base.enums;

import lombok.Getter;

/**
 * 执行结果枚举类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 13:18
 */
@Getter
public enum ExecResultEnum {
                            /**
                             * 执行成功
                             */
                            SUCCESS(0, "SUCCESS", "执行成功"),
                            /**
                             * 执行失败
                             */
                            FAILED(1, "FAILED", "执行失败"),
                            /**
                             * 未开始
                             */
                            NOT_STARTED(2, "-", "未开始");

    private int    id;
    private String result;
    private String desc;

    ExecResultEnum(int id, String result, String desc) {
        this.id = id;
        this.result = result;
        this.desc = desc;
    }
}
