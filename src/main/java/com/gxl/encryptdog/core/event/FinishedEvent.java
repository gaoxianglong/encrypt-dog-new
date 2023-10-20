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

import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.utils.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 完成事件
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/20 15:02
 */
@Setter
@Getter
public class FinishedEvent extends BaseEvent {
    @Serial
    private static final long serialVersionUID = -7753951150665735283L;

    /**
     * 整体执行耗时
     */
    private String            timeConsuming;
    /**
     * 结束音播放器
     */
    private Player            player;
    /**
     * 执行结果上下文
     */
    private ResultContext     resultContext;

    public FinishedEvent(String id, long timestamp) {
        super(id, timestamp);
    }
}
