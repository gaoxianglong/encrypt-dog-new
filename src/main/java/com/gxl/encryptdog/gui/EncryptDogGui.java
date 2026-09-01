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

package com.gxl.encryptdog.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.gxl.encryptdog.gui.application.dto.EncryptFormDTO;
import com.gxl.encryptdog.gui.interfaces.swing.EncryptDogFrame;
import com.gxl.encryptdog.gui.interfaces.swing.LogoUtil;
import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Taskbar;
import java.awt.image.BaseMultiResolutionImage;

/**
 * GUI启动器:安装FlatLaf暗色主题并打开主窗口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class EncryptDogGui {
    private EncryptDogGui() {
    }

    /**
     * 启动GUI
     * @param prefill 预填表单数据,--gui后剩余命令行参数解析而来,可为null
     */
    public static void launch(EncryptFormDTO prefill) {
        // 安装FlatLaf暗色主题
        FlatDarkLaf.setup();
        // 滚动条主题化:滑块ACCENT紫,悬停/按下提亮(全局生效,含文件列表/执行列表/下拉弹窗滚动条)
        UIManager.put("ScrollBar.thumbColor", UiConstants.ACCENT);
        UIManager.put("ScrollBar.thumb", UiConstants.ACCENT);
        UIManager.put("ScrollBar.thumbHighlight", UiConstants.ACCENT_BRIGHT);
        UIManager.put("ScrollBar.thumbDarkShadow", UiConstants.ACCENT);
        UIManager.put("ScrollBar.hoverThumbColor", UiConstants.ACCENT_BRIGHT);
        UIManager.put("ScrollBar.pressedThumbColor", UiConstants.ACCENT_BRIGHT);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EncryptDogFrame frame = new EncryptDogFrame();
                if (prefill != null) {
                    frame.prefill(prefill);
                }
                frame.setVisible(true);
                // Dock图标:必须在EDT且窗口可见后设置(macOS AWT在窗口首次显示时才关联Dock,过早设置会被激活过程重置)
                installDockIcon();
            }
        });
    }

    /**
     * 设置Dock图标:128px+256px@2x双档多分辨率(macOS Dock ~128pt渲染),失败静默降级
     */
    private static void installDockIcon() {
        try {
            if (!Taskbar.isTaskbarSupported()) {
                return;
            }
            var icon128 = LogoUtil.loadRoundedImage(UiConstants.DOCK_LOGO_RESOURCE, 128);
            var icon256 = LogoUtil.loadRoundedImage(UiConstants.DOCK_LOGO_RESOURCE, 256);
            if (icon128 == null || icon256 == null) {
                return;
            }
            Taskbar.getTaskbar().setIconImage(new BaseMultiResolutionImage(icon128, icon256));
        } catch (Throwable e) {
            // 无桌面环境/Taskbar不可用等环境性问题,静默降级
        }
    }

    public static void main(String[] args) {
        launch(null);
    }
}
