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
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 任务进度事件
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/15 22:55
 */
@Getter
@Setter
public class ProgressEvent extends BaseEvent {
    @Serial
    private static final long serialVersionUID = -7858022818228317575L;
    /**
     * 当前进度
     */
    private String            progress;

    public ProgressEvent(String id, long timestamp, EncryptContext encryptContext) {
        super(id, timestamp, encryptContext);
    }
}
