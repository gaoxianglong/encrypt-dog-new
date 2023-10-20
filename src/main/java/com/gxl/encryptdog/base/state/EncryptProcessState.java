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

package com.gxl.encryptdog.base.state;

import com.gxl.encryptdog.base.enums.EncryptStateEnum;
import com.gxl.encryptdog.base.error.ListenerException;
import com.gxl.encryptdog.base.error.StateException;
import com.gxl.encryptdog.core.operation.EncryptContext;

/**
 * 加/解密执行状态机接口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 13:51
 */
public interface EncryptProcessState {
    /**
     * 加/解密执行状态流转
     * @param encryptContext
     * @param to
     * @throws StateException
     * @throws ListenerException
     */
    void setState(EncryptContext encryptContext, EncryptStateEnum to) throws StateException, ListenerException;
}
