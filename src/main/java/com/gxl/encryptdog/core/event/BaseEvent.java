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

package com.gxl.encryptdog.core.event;

import com.gxl.encryptdog.core.operation.EncryptContext;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 事件基类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/13 11:07
 */
@Data
public abstract class BaseEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 6201121198478573836L;

    /**
     * 事件ID
     */
    private final String      id;

    /**
     * 事件产生时间
     */
    private final long        timestamp;

    /**
     * 加/解密操作上下文
     */
    private EncryptContext encryptContext;

    public BaseEvent(String id, long timestamp, EncryptContext encryptContext) {
        this.id = id;
        this.timestamp = timestamp;
        this.encryptContext = encryptContext;
    }

    public BaseEvent(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }
}