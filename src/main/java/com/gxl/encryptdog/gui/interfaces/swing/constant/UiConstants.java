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

package com.gxl.encryptdog.gui.interfaces.swing.constant;

import java.awt.Color;

/**
 * 界面视觉与动画常量
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public final class UiConstants {
    /**
     * 应用名称
     */
    public static final String APP_NAME                 = "EncryptionDog";
    /**
     * 应用版本
     */
    public static final String VERSION                  = "v2.0.3-RELEASE";
    /**
     * logo资源路径
     */
    public static final String LOGO_RESOURCE            = "logo.png";
    /**
     * 表单头部logo显示尺寸
     */
    public static final int    LOGO_FORM_SIZE           = 40;
    /**
     * 标题栏logo显示尺寸
     */
    public static final int    LOGO_TITLE_SIZE          = 28;
    /**
     * 标题栏应用名称字号(与logo同高平行)
     */
    public static final int    TITLE_BAR_FONT_SIZE      = 22;
    /**
     * 文件空态图标资源路径
     */
    public static final String FILE_EMPTY_RESOURCE      = "file-empty.png";
    /**
     * 执行页返回图标资源路径
     */
    public static final String HOME_RESOURCE            = "home-fill.png";
    /**
     * 执行结果成功图标资源路径
     */
    public static final String RESULT_SUCCESS_RESOURCE  = "result-success.png";
    /**
     * 执行结果失败图标资源路径
     */
    public static final String RESULT_ERROR_RESOURCE    = "result-error.png";
    /**
     * 拖拽框空态图标显示尺寸
     */
    public static final int    FILE_EMPTY_ICON_SIZE     = 48;
    /**
     * 窗口宽度（逻辑像素）
     */
    public static final int    WINDOW_WIDTH             = 760;
    /**
     * 窗口高度（逻辑像素）
     */
    public static final int    WINDOW_HEIGHT            = 760;
    /**
     * 标题栏高度（逻辑像素）
     */
    public static final int    TITLE_BAR_HEIGHT         = 46;
    /**
     * 窗口圆角半径
     */
    public static final int    WINDOW_ARC               = 28;
    /**
     * 卡片宽度
     */
    public static final int    CARD_WIDTH               = 580;
    /**
     * 卡片高度
     */
    public static final int    CARD_HEIGHT              = 600;
    /**
     * 卡片圆角半径
     */
    public static final int    CARD_ARC                 = 24;
    /**
     * 卡片左右内边距
     */
    public static final int    CARD_PADDING_X           = 36;
    /**
     * 粒子数量
     */
    public static final int    PARTICLE_COUNT           = 300;
    /**
     * 星座连线距离阈值（像素）
     */
    public static final int    LINK_DISTANCE            = 110;
    /**
     * 鼠标影响半径（像素）
     */
    public static final int    MOUSE_INFLUENCE_RADIUS   = 90;
    /**
     * 鼠标推动强度（像素/帧）
     */
    public static final double MOUSE_PUSH_STRENGTH      = 1.6;
    /**
     * 动画帧间隔（毫秒，约 60fps）
     */
    public static final int    FRAME_DELAY_MS           = 16;
    /**
     * 粒子聚集阶段时长（毫秒）
     */
    public static final int    BURST_GATHER_MS          = 550;
    /**
     * 粒子爆发飞散阶段时长（毫秒）
     */
    public static final int    BURST_FLY_MS             = 900;
    /**
     * 粒子基础漂移速度下限
     */
    public static final double PARTICLE_MIN_SPEED       = 0.25;
    /**
     * 粒子基础漂移速度上限
     */
    public static final double PARTICLE_MAX_SPEED       = 0.8;
    /**
     * 粒子半径下限
     */
    public static final double PARTICLE_MIN_RADIUS      = 0.7;
    /**
     * 粒子半径上限
     */
    public static final double PARTICLE_MAX_RADIUS      = 2.2;
    /**
     * 粒子亮度下限
     */
    public static final double PARTICLE_MIN_BRIGHTNESS  = 0.15;
    /**
     * 粒子亮度上限
     */
    public static final double PARTICLE_MAX_BRIGHTNESS  = 1.0;
    /**
     * 星座连线最大透明度
     */
    public static final int    LINK_MAX_ALPHA           = 110;
    /**
     * 输入框高度
     */
    public static final int    INPUT_HEIGHT             = 40;
    /**
     * 主按钮高度
     */
    public static final int    PRIMARY_BUTTON_HEIGHT    = 44;
    /**
     * 任务标题字号
     */
    public static final int    TASK_TITLE_FONT_SIZE     = 22;
    /**
     * 标题字号
     */
    public static final int    TITLE_FONT_SIZE          = 22;
    /**
     * 副标题字号
     */
    public static final int    SUBTITLE_FONT_SIZE       = 13;
    /**
     * 正文字号
     */
    public static final int    BODY_FONT_SIZE           = 14;
    /**
     * 小字号
     */
    public static final int    SMALL_FONT_SIZE          = 12;
    /**
     * 背景渐变起始色
     */
    public static final Color  BG_TOP                   = new Color(0x0B0B1E);
    /**
     * 背景渐变结束色
     */
    public static final Color  BG_BOTTOM                = new Color(0x23104A);
    /**
     * 粒子亮色
     */
    public static final Color  PARTICLE_BRIGHT          = new Color(0xBDC9FF);
    /**
     * 粒子暗色
     */
    public static final Color  PARTICLE_DIM             = new Color(0x5A6BD6);
    /**
     * 星座连线颜色
     */
    public static final Color  LINK_COLOR               = new Color(0x6C7BFF);
    /**
     * 卡片填充色（半透明白）
     */
    public static final Color  CARD_FILL                = new Color(255, 255, 255, 20);
    /**
     * 卡片描边色（半透明白）
     */
    public static final Color  CARD_BORDER              = new Color(255, 255, 255, 48);
    /**
     * 卡片高光描边色
     */
    public static final Color  CARD_HIGHLIGHT           = new Color(255, 255, 255, 70);
    /**
     * 卡片阴影色
     */
    public static final Color  CARD_SHADOW              = new Color(0, 0, 0, 90);
    /**
     * 主题强调色
     */
    public static final Color  ACCENT                   = new Color(0x6C63FF);
    /**
     * 主题强调亮色
     */
    public static final Color  ACCENT_BRIGHT            = new Color(0x8E7BFF);
    /**
     * 错误红色
     */
    public static final Color  ERROR_RED                = new Color(0xFF5C6C);
    /**
     * 成功绿色
     */
    public static final Color  SUCCESS_GREEN            = new Color(0x52D39B);
    /**
     * 标题栏背景色
     */
    public static final Color  TITLE_BAR_BG             = new Color(0x10102A);
    /**
     * 标题栏按钮悬停色
     */
    public static final Color  TITLE_BAR_HOVER          = new Color(255, 255, 255, 28);
    /**
     * 标题栏关闭按钮悬停色
     */
    public static final Color  TITLE_BAR_CLOSE_HOVER    = new Color(0xE05555);
    /**
     * 主文本色
     */
    public static final Color  TEXT_PRIMARY             = new Color(0xEDEDFF);
    /**
     * 次要文本色
     */
    public static final Color  TEXT_SECONDARY           = new Color(0x9B9BC7);
    /**
     * 按钮文本色
     */
    public static final Color  BUTTON_TEXT              = Color.WHITE;
    /**
     * 禁用按钮背景色
     */
    public static final Color  BUTTON_DISABLED          = new Color(0x4A4A6E);

    private UiConstants() {
    }
}
