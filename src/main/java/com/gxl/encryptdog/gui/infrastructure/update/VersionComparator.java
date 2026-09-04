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

package com.gxl.encryptdog.gui.infrastructure.update;

/**
 * 版本号归一化与比较纯函数:剥离v前缀与-RELEASE后缀后逐段数值比较
 * (不走Runtime.Version.parse:当前运行JDK对"2.1.0"形态的版本串解析直接抛异常),
 * 任一侧无法解析均视为无新版本(静默降级,不抛异常)
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/4 15:00
 */
public final class VersionComparator {
    /**
     * -RELEASE后缀
     */
    private static final String RELEASE_SUFFIX = "-RELEASE";

    private VersionComparator() {
    }

    /**
     * 归一化版本号:去除首尾空白、剥离v/V前缀与-RELEASE后缀
     *
     * @param version 原始版本号(如v2.1.0/2.0.5-RELEASE)
     * @return 归一化后的版本号(如2.1.0/2.0.5),入参为null返回null
     */
    public static String normalize(String version) {
        if (null == version) {
            return null;
        }
        var result = version.trim();
        if (result.startsWith("v") || result.startsWith("V")) {
            result = result.substring(1);
        }
        if (result.endsWith(RELEASE_SUFFIX)) {
            result = result.substring(0, result.length() - RELEASE_SUFFIX.length());
        }
        return result;
    }

    /**
     * latest是否高于current
     *
     * @param current 当前运行时版本(如2.0.5)
     * @param latest 远端最新版本tag(如v2.1.0)
     * @return true=有更新;两侧任一为null或不可解析时返回false(视为无新版本)
     */
    public static boolean isNewer(String current, String latest) {
        if (null == current || null == latest) {
            return false;
        }
        try {
            return compare(normalize(current), normalize(latest)) < 0;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 逐段数值比较两个点分版本号(段数不足补0,如2.1 vs 2.1.0相等)
     *
     * @param a 版本a
     * @param b 版本b
     * @return a小于b返回负数,相等返回0,a大于b返回正数
     */
    private static int compare(String a, String b) {
        var aSegments = a.split("\\.");
        var bSegments = b.split("\\.");
        var length = Math.max(aSegments.length, bSegments.length);
        for (var i = 0; i < length; i++) {
            var aValue = i < aSegments.length ? parseSegment(aSegments[i]) : 0;
            var bValue = i < bSegments.length ? parseSegment(bSegments[i]) : 0;
            if (aValue != bValue) {
                return aValue < bValue ? -1 : 1;
            }
        }
        return 0;
    }

    /**
     * 解析单个版本段:取前导数字部分(容忍0-RELEASE残留与beta等后缀),纯非数字段抛异常
     *
     * @param segment 版本段
     * @return 段数值
     */
    private static int parseSegment(String segment) {
        var digits = new StringBuilder();
        for (var c : segment.trim().toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else {
                break;
            }
        }
        var text = digits.toString();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("non-numeric segment: " + segment);
        }
        return Integer.parseInt(text);
    }
}
