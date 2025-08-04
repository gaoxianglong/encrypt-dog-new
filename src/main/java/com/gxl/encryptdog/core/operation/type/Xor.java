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
package com.gxl.encryptdog.core.operation.type;

import com.gxl.encryptdog.base.enums.EncryptTypeEnum;

/**
 * XOR混淆加密标记接口
 * 在-d模式下,如果秘钥错误也能够正常进行异或运算,但是会损坏源文件,慎用
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2024/9/4 15:39
 */
public interface Xor {
    /**
     * 加密算法名称
     */
    String ALGORITHM_NAME = EncryptTypeEnum.XOR.getAlgorithmName();
}
