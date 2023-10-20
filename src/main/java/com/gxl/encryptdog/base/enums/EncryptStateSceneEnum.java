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
 * 加/解密执行状态场景枚举类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 13:54
 */
@Getter
public enum EncryptStateSceneEnum {
                                   /**
                                    * 执行加/解密场景
                                    */
                                   ENCRYPT_SCENE(0, EncryptStateEnum.WAITING, EncryptStateEnum.RUNNING, "执行加/解密场景"),
                                   /**
                                    * 加/解密结束场景
                                    */
                                   ENCRYPT_FINISHED_SCENE(1, EncryptStateEnum.RUNNING, EncryptStateEnum.FINISHED, "加/解密结束场景");

    private Integer          code;
    /**
     * 当前的加/解密状态
     */
    private EncryptStateEnum from;
    /**
     * 目标加/解密状态
     */
    private EncryptStateEnum to;
    private String           desc;

    EncryptStateSceneEnum(Integer code, EncryptStateEnum from, EncryptStateEnum to, String desc) {
        this.code = code;
        this.from = from;
        this.to = to;
        this.desc = desc;
    }

    /**
     * 场景合法性判断
     * @param from
     * @param to
     * @return
     */
    public static EncryptStateSceneEnum checkScene(EncryptStateEnum from, EncryptStateEnum to) {
        for (EncryptStateSceneEnum scene : EncryptStateSceneEnum.values()) {
            if (scene.from == from && scene.to == to) {
                return scene;
            }
        }
        return null;
    }
}
