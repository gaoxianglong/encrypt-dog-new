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

package com.gxl.encryptdog.utils.player;

import com.gxl.encryptdog.base.error.ResourceException;

/**
 * 播放器接口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/26 23:44
 */
public interface Player {
    /**
     * 缺省缓冲区大小
     */
    int DEFAULT_BUF_SIZE = 1024;

    /**
     * 加解密操作完成后播放提示音
     * @param path
     * @throws ResourceException
     */
    void play(String path) throws ResourceException;
}
