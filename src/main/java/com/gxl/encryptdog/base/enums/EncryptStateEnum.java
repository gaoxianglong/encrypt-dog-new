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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加/解密执行状态
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 13:55
 */
@Getter
public enum EncryptStateEnum {
                              /**
                               * 初始状态,等待中
                               */
                              WAITING(0, "WAITING", "初始过程"),
                              /**
                               * 执行中
                               */
                              RUNNING(1, "RUNNING", "执行中"),
                              /**
                               * 已完成
                               */
                              FINISHED(2, "FINISHED", "已完成");

    /**
     * code
     */
    private Integer                       code;
    /**
     * 状态
     */
    private String                        state;
    /**
     * 描述信息
     */
    private String                        desc;

    static Map<Integer, EncryptStateEnum> maps = new ConcurrentHashMap<>() {
                                                   private static final long serialVersionUID = -6390379820870308191L;

                                                   {
                                                       for (var e : EncryptStateEnum.values()) {
                                                           put(e.getCode(), e);
                                                       }
                                                   }
                                               };

    /**
     * 根据code获取枚举类
     * @param code
     * @return
     */
    public static EncryptStateEnum getEncryptProcessStateEnum(Integer code) {
        return maps.get(code);
    }

    EncryptStateEnum(Integer code, String state, String desc) {
        this.code = code;
        this.state = state;
        this.desc = desc;
    }
}
