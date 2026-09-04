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
import com.gxl.encryptdog.gui.interfaces.swing.ThemedConfirmDialog;
import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;
import com.gxl.encryptdog.gui.interfaces.swing.tray.SingleInstanceGuard;
import com.gxl.encryptdog.gui.interfaces.swing.tray.TrayManager;
import com.gxl.encryptdog.gui.interfaces.swing.update.UpdateUiBridge;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.Desktop;
import java.awt.Taskbar;
import java.awt.image.BaseMultiResolutionImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GUI启动器:安装FlatLaf暗色主题并打开主窗口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class EncryptDogGui {
    /**
     * 主窗口(建窗后赋值,供odoc合并路径访问,仅EDT读写)
     */
    private static volatile EncryptDogFrame frame;
    /**
     * 双击打开文件的pending缓冲(odoc事件与建窗时序解耦:事件可能先于建窗到达)
     */
    private static final List<File>         pendingOpenedFiles = new ArrayList<>();

    private EncryptDogGui() {
    }

    /**
     * 启动GUI
     * @param prefill 预填表单数据,--gui后剩余命令行参数解析而来,可为null
     */
    public static void launch(EncryptFormDTO prefill) {
        // 单例锁:已有实例在跑则唤醒其窗口后本进程退出,陌生端口占用降级正常启动
        SingleInstanceGuard.ensureSingle();
        // 打开文件事件处理:必须在EDT建窗前注册(冷启动双击的事件在JVM启动瞬间到达,
        // AWT原生层排队到handler注册时投递,注册越早丢事件风险越小)
        registerOpenFileHandler();
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
                frame = new EncryptDogFrame();
                if (prefill != null) {
                    frame.prefill(prefill);
                }
                // 建窗路径尾部合并:双击事件若先于建窗到达,pending在此兜底合并
                mergeOpenedFiles();
                // 托盘挂载成功才切换✕为隐藏语义,失败保持退出语义(降级矩阵)
                if (TrayManager.install(frame)) {
                    frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                    SingleInstanceGuard.setWakeAction(TrayManager::restoreWindow);
                } else {
                    // 无托盘:单例唤醒直接恢复窗口
                    SingleInstanceGuard.setWakeAction(() -> restoreFrame(frame));
                }
                frame.setVisible(true);
                // Dock图标:必须在EDT且窗口可见后设置(macOS AWT在窗口首次显示时才关联Dock,过早设置会被激活过程重置)
                installDockIcon();
                // 更新检查:托盘安装后(无论成败)启动一次后台检查,托盘降级时弹窗与下载仍可用
                UpdateUiBridge.init(frame);
            }
        });
    }

    /**
     * 注册macOS打开文件事件处理(双击/打开方式打开的.dog走odoc事件而非argv):
     * 回调把文件追加进pending后触发EDT合并。二次注册/平台不支持时静默降级
     * (jar形态与终端模式不触达此处,不受影响)
     */
    private static void registerOpenFileHandler() {
        try {
            Desktop.getDesktop().setOpenFileHandler(e -> {
                synchronized (pendingOpenedFiles) {
                    pendingOpenedFiles.addAll(e.getFiles());
                }
                SwingUtilities.invokeLater(EncryptDogGui::mergeOpenedFiles);
            });
        } catch (Throwable e) {
            // 打开文件事件不支持(非macOS/无桌面环境等),静默降级为仅参数预填路径
        }
    }

    /**
     * 合并pending的打开文件到表单(仅EDT调用):窗口未建时直接返回等建窗路径兜底;
     * 存在文件时以解密模式整体预填并唤回窗口;任务执行中(干活态)不预填,
     * 仅唤回窗口并弹提示框,进行中任务不受影响
     */
    private static void mergeOpenedFiles() {
        var current = frame;
        if (current == null) {
            return;
        }
        List<File> files;
        synchronized (pendingOpenedFiles) {
            if (pendingOpenedFiles.isEmpty()) {
                return;
            }
            files = new ArrayList<>(pendingOpenedFiles);
            pendingOpenedFiles.clear();
        }
        if (TrayManager.isBusy()) {
            // 干活态:清空pending、唤回窗口、提示任务进行中(文件不预填)
            TrayManager.restoreWindow();
            ThemedConfirmDialog.showMessage(current, "Task in progress",
                    "Files opened while a task is running are not loaded", "OK");
            return;
        }
        // 空闲态:双击.dog是明确解密意图,以解密模式整体预填(密钥仍由用户输入)
        var form = new EncryptFormDTO();
        form.setEncrypt(false);
        form.setSourceFilePaths(files.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
        current.prefill(form);
        // 唤回窗口(隐藏态恢复、可见态聚焦),与单例唤醒共用同一恢复路径
        TrayManager.restoreWindow();
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

    /**
     * 恢复窗口前台显示(无托盘降级场景的单例唤醒路径)
     * @param target 主窗口
     */
    private static void restoreFrame(EncryptDogFrame target) {
        SwingUtilities.invokeLater(() -> {
            target.setVisible(true);
            if (target.getState() == JFrame.ICONIFIED) {
                target.setState(JFrame.NORMAL);
            }
            target.toFront();
            target.requestFocus();
        });
    }

    public static void main(String[] args) {
        launch(null);
    }
}
