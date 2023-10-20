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

package com.gxl.encryptdog.base.common;

/**
 * 相关静态常量类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/21 22:13
 */
public class Constants {
    /**
     * 换行符
     */
    public static final String WRAP                            = System.getProperty("line.separator");
    /**
     * 缺省扫描Operation接口的类路径
     */
    public static final String DEFAULT_SCAN_OPERATION_CLS_PATH = "com.gxl.encryptdog.core.operation";
    /**
     * 缺省扫描BaseListener接口的类路径
     */
    public static final String DEFAULT_SCAN_LISTENER_CLS_PATH  = "com.gxl.encryptdog.core.event.listener";
    /**
     * 缺省的配置文件路径
     */
    public static final String DEFAULT_CONFIG_PATH             = "properties/dog.properties";
    /**
     * 缺省提示音屏文件路径
     */
    public static final String DEFAULT_AUDIOS_PATH             = "audio/finish.wav";
    /**
     * 默认密码长度
     */
    public static final int    DEFAULT_PWD_LENGTH              = 6;
    /**
     * 冒号
     */
    public static final String COLON                           = ":";
    /**
     * 路径分隔符
     */
    public static final String FILE_SEPARATOR                  = System.getProperty("file.separator");
    /**
     * 句号分隔符
     */
    public static final String PERIOD_SEPARATOR                = ".";
    /**
     * 加密文件缺省后缀
     */
    public static final String DEFAULT_SUFFIX                  = ".dog";
    /**
     * 空格符
     */
    public static final String SPACE                           = "\\s";
    /**
     * 1GB对应的字节大小
     */
    public static final int    GB                              = 0x40000000;
    /**
     * 1MB对应的字节大小
     */
    public static final int    MB                              = 0X100000;
    /**
     * 一小时所对应的秒数
     */
    public static final int    HOUR                            = 0xe10;
    /**
     * 一分钟所对应的秒数
     */
    public static final int    MINUTE                          = 0x3c;
}
