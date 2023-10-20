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

package com.gxl.encryptdog.core.shell.view;

import com.gxl.encryptdog.base.enums.EncryptStateEnum;
import com.gxl.encryptdog.core.operation.proxy.params.DashboardViewState;

import java.util.Comparator;

/**
 * 结果集比较器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/16 14:04
 */
public class ResultComparator implements Comparator<DashboardViewState> {
    @Override
    public int compare(DashboardViewState o1, DashboardViewState o2) {
        var s1 = o1.getState();
        var s2 = o2.getState();

        // RUNNING排在WAITING前面
        if (s1 == EncryptStateEnum.RUNNING && s2 == EncryptStateEnum.WAITING) {
            return -1;
        }
        // WAITING排在RUNNING后面
        else if (s1 == EncryptStateEnum.WAITING && s2 == EncryptStateEnum.RUNNING) {
            return 1;
        }
        // RUNNING排在FINISHED前面
        else if (s1 == EncryptStateEnum.RUNNING && s2 == EncryptStateEnum.FINISHED) {
            return -1;
        }
        // FINISHED排在RUNNING后面
        else if (s1 == EncryptStateEnum.FINISHED && s2 == EncryptStateEnum.RUNNING) {
            return 1;
        }
        // 其他情况按字母顺序排序
        return s1.getState().compareTo(s2.getState());
    }
}
