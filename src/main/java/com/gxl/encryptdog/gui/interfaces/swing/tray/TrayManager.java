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

package com.gxl.encryptdog.gui.interfaces.swing.tray;

import com.gxl.encryptdog.gui.application.dto.OperationProgressDTO;
import com.gxl.encryptdog.gui.application.dto.OperationProgressDTO.FileProgress;
import com.gxl.encryptdog.gui.application.dto.OperationResultDTO;
import com.gxl.encryptdog.gui.application.dto.OperationResultDTO.FileResult;
import com.gxl.encryptdog.gui.interfaces.swing.LogoUtil;
import com.gxl.encryptdog.gui.interfaces.swing.ThemedConfirmDialog;
import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.desktop.AppReopenedEvent;
import java.awt.desktop.AppReopenedListener;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.image.BaseMultiResolutionImage;
import java.io.File;

/**
 * 单进程菜单栏托盘:挂载图标、空闲/干活二态菜单与tooltip、窗口恢复、
 * 退出确认、最近输出定位与完成通知。安装失败时所有钩子短路(降级矩阵)。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/2 15:00
 */
public class TrayManager {
    /**
     * 菜单栏图标尺寸(单倍/2x retina双档)
     */
    private static final int    TRAY_ICON_SIZE      = 22;
    private static final int    TRAY_ICON_SIZE_2X   = 44;
    /**
     * 干活态tooltip/状态行刷新节流(进度事件按块高频触发)
     */
    private static final long   THROTTLE_MS         = 500;
    /**
     * 成功结果枚举值(core ExecResultEnum)
     */
    private static final String SUCCESS_RESULT      = "SUCCESS";
    private static final String FINISHED_STATE      = "FINISHED";
    /**
     * 菜单文案
     */
    private static final String MENU_SHOW           = "Show";
    private static final String MENU_REVEAL         = "Reveal last output";
    private static final String MENU_QUIT           = "Quit";
    /**
     * 更新项文案(全英文无emoji,与既有菜单语言一致)
     */
    private static final String MENU_UPDATE_CHECK       = "Check for updates";
    private static final String MENU_UPDATE_CHECKING    = "Checking for updates...";
    private static final String MENU_UPDATE_AVAILABLE   = "Update available %s";
    private static final String MENU_UPDATE_DOWNLOADING = "Downloading %s · %d%%";
    private static final String MENU_UPDATE_OPEN        = "Open downloaded update";

    /**
     * 托盘状态:空闲/干活中
     */
    private enum TrayState {
        IDLE, WORKING
    }

    /**
     * 更新项插槽状态(与更新能力共享的展示状态机)
     */
    public enum UpdateItemState {
        /**
         * 常态:可点击检查
         */
        CHECK,
        /**
         * 检查进行中(禁用)
         */
        CHECKING,
        /**
         * 已发现新版本且未下载
         */
        AVAILABLE,
        /**
         * 下载进行中(禁用)
         */
        DOWNLOADING,
        /**
         * 下载完成且缓存存在
         */
        DOWNLOADED
    }

    /**
     * 更新项点击回调(由更新能力桥接注册,托盘不反向依赖网络层)
     */
    public interface UpdateActionHandler {
        /**
         * 点击Check for updates
         */
        void onCheckForUpdates();

        /**
         * 点击Update available {version}
         */
        void onUpdateAvailable();

        /**
         * 点击Open downloaded update
         */
        void onOpenDownloaded();
    }

    /**
     * 当前状态
     */
    private static volatile TrayState state            = TrayState.IDLE;
    /**
     * 所属主窗口(install成功后赋值)
     */
    private static volatile JFrame    frame;
    /**
     * 菜单栏图标与弹出菜单
     */
    private static volatile TrayIcon  trayIcon;
    private static PopupMenu          menu;
    /**
     * 干活态状态行(禁用菜单项)
     */
    private static MenuItem           statusItem;
    /**
     * 最近一次成功输出文件(Reveal last output定位目标)
     */
    private static volatile File      lastOutputFile;
    /**
     * 上次进度刷新时间戳(节流)
     */
    private static volatile long      lastProgressUpdateMs;
    /**
     * 托盘是否安装成功
     */
    private static volatile boolean   installed;
    /**
     * 更新项插槽状态
     */
    private static volatile UpdateItemState updateItemState = UpdateItemState.CHECK;
    /**
     * 更新项版本号(已归一化)
     */
    private static volatile String    updateVersion;
    /**
     * 下载进度百分比,-1表示未知
     */
    private static volatile int       updatePct = -1;
    /**
     * 已下载DMG缓存文件(下载完成态渲染时校验存在性)
     */
    private static volatile File      updateDmg;
    /**
     * 干活态最近一次状态文案(更新项刷新时复用)
     */
    private static String             workingText = "";
    /**
     * 更新项点击回调
     */
    private static volatile UpdateActionHandler updateActionHandler;
    /**
     * 回到空闲态钩子(更新能力挂起弹窗的补弹入口)
     */
    private static volatile Runnable  idleHook;

    private TrayManager() {
    }

    /**
     * 挂载托盘图标与macOS回调,失败静默返回false(降级矩阵:不切换✕语义)。
     * 仅在EDT调用。
     *
     * @param owner 主窗口
     * @return 是否安装成功
     */
    public static boolean install(JFrame owner) {
        if (installed) {
            return true;
        }
        try {
            if (!SystemTray.isSupported()) {
                return false;
            }
            var icon22 = LogoUtil.loadLogo(TRAY_ICON_SIZE);
            var icon44 = LogoUtil.loadLogo(TRAY_ICON_SIZE_2X);
            if (icon22 == null || icon44 == null) {
                return false;
            }
            frame = owner;
            trayIcon = new TrayIcon(new BaseMultiResolutionImage(icon22.getImage(), icon44.getImage()), UiConstants.APP_NAME);
            trayIcon.setImageAutoSize(false);
            menu = new PopupMenu();
            SystemTray.getSystemTray().add(trayIcon);
            registerMacCallbacks();
            installed = true;
            applyIdle();
            return true;
        } catch (Throwable e) {
            // 托盘不可用/图标缺失等环境性问题,静默降级:✕保持退出语义
            return false;
        }
    }

    /**
     * 执行开始:切入干活态并展示初始状态行
     *
     * @param totalFiles 总文件数
     */
    public static void onOperationStart(int totalFiles) {
        if (!installed) {
            return;
        }
        state = TrayState.WORKING;
        SwingUtilities.invokeLater(() -> applyWorking(String.format("Encrypting 0/%d · 0%%", totalFiles)));
    }

    /**
     * 进度刷新:调度线程触发,内部切EDT并节流
     *
     * @param progress 进度快照
     */
    public static void onProgress(OperationProgressDTO progress) {
        if (!installed) {
            return;
        }
        var now = System.currentTimeMillis();
        if (now - lastProgressUpdateMs < THROTTLE_MS) {
            return;
        }
        lastProgressUpdateMs = now;
        var text = buildProgressText(progress);
        SwingUtilities.invokeLater(() -> applyWorking(text));
    }

    /**
     * 执行完成(含部分失败)或执行异常:回空闲态,窗口隐藏时弹系统通知。
     * result为null表示执行异常路径。
     *
     * @param result 执行结果
     */
    public static void onOperationFinished(OperationResultDTO result) {
        if (!installed) {
            return;
        }
        state = TrayState.IDLE;
        SwingUtilities.invokeLater(() -> {
            if (result != null) {
                // 记录最近一次成功输出(取列表最后一个成功条目)
                for (FileResult fr : result.getFileResults()) {
                    if (SUCCESS_RESULT.equals(fr.getResult()) && fr.getTargetFile() != null
                            && !fr.getTargetFile().isEmpty()) {
                        lastOutputFile = new File(fr.getTargetFile());
                    }
                }
            }
            applyIdle();
            runIdleHook();
            if (frame != null && !frame.isVisible()) {
                trayIcon.displayMessage(UiConstants.APP_NAME, buildSummary(result), TrayIcon.MessageType.NONE);
            }
        });
    }

    /**
     * 恢复窗口前台显示(托盘Show/Dock点击/单例唤醒三来源共用)
     */
    public static void restoreWindow() {
        if (!installed || frame == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            if (frame.getState() == JFrame.ICONIFIED) {
                frame.setState(JFrame.NORMAL);
            }
            frame.toFront();
            frame.requestFocus();
        });
    }

    /**
     * 是否处于干活态(任务执行中),供更新弹窗延迟判断使用
     * @return true=任务执行中
     */
    public static boolean isBusy() {
        return state == TrayState.WORKING;
    }

    /**
     * 设置更新项插槽状态并刷新菜单(托盘未安装时静默忽略)
     *
     * @param itemState 插槽状态
     * @param version 版本号(仅AVAILABLE/DOWNLOADING使用,可空)
     * @param pct 下载进度百分比(仅DOWNLOADING使用,-1表示未知)
     */
    public static void setUpdateItemState(UpdateItemState itemState, String version, int pct) {
        setUpdateItemState(itemState, version, pct, null);
    }

    /**
     * 设置更新项插槽状态并刷新菜单(含已下载DMG文件,供下载完成态校验缓存存在性)
     *
     * @param itemState 插槽状态
     * @param version 版本号(仅AVAILABLE/DOWNLOADING使用,可空)
     * @param pct 下载进度百分比(仅DOWNLOADING使用,-1表示未知)
     * @param dmg 已下载DMG文件(仅DOWNLOADED使用,可空)
     */
    public static void setUpdateItemState(UpdateItemState itemState, String version, int pct, File dmg) {
        updateDmg = dmg;
        if (!installed) {
            return;
        }
        updateItemState = itemState;
        updateVersion = version;
        updatePct = pct;
        SwingUtilities.invokeLater(() -> {
            if (state == TrayState.WORKING) {
                applyWorking(workingText);
            } else {
                applyIdle();
            }
        });
    }

    /**
     * 注册更新项点击回调(由更新能力桥接注册)
     * @param handler 点击回调
     */
    public static void setUpdateActionHandler(UpdateActionHandler handler) {
        updateActionHandler = handler;
    }

    /**
     * 注册回到空闲态钩子:每次任务结束回到空闲态时触发(更新能力用于补弹挂起的更新弹窗)
     * @param hook 空闲钩子
     */
    public static void setIdleHook(Runnable hook) {
        idleHook = hook;
    }

    /**
     * 弹出系统通知(托盘未安装时静默忽略),消息全英文
     * @param message 通知内容
     */
    public static void notify(String message) {
        if (!installed || null == trayIcon) {
            return;
        }
        SwingUtilities.invokeLater(() -> trayIcon.displayMessage(UiConstants.APP_NAME, message, TrayIcon.MessageType.NONE));
    }

    /**
     * Reveal last output:打开最近成功输出文件所在目录并选中该文件,失败静默
     */
    public static void reveal() {
        var target = lastOutputFile;
        if (target == null) {
            return;
        }
        try {
            Desktop.getDesktop().browseFileDirectory(target);
        } catch (Throwable e) {
            // 目标文件已被移动/删除等,静默失败不影响程序
        }
    }

    /**
     * 请求退出(托盘Quit与Cmd+Q共用):空闲直接退出,干活中弹确认浮层。
     * 确认后直接终止进程(✕已改为隐藏语义,dispose无法退出)。
     */
    public static void requestQuit() {
        if (!installed) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (state == TrayState.WORKING) {
                // 确认浮层依附主窗口,窗口隐藏时先恢复保证可见
                restoreWindow();
                var confirmed = ThemedConfirmDialog.show(frame, "Quit EncryptDog",
                        "Tasks are still running",
                        "Files being processed may be left incomplete",
                        "Quit anyway", "Keep working");
                if (!confirmed) {
                    return;
                }
            }
            System.exit(0);
        });
    }

    /**
     * 刷新空闲态菜单(Show/Reveal last output/更新检查项/Quit)与tooltip
     */
    private static void applyIdle() {
        menu.removeAll();
        var showItem = new MenuItem(MENU_SHOW);
        showItem.addActionListener(e -> restoreWindow());
        menu.add(showItem);
        var revealItem = new MenuItem(MENU_REVEAL);
        revealItem.setEnabled(lastOutputFile != null);
        revealItem.addActionListener(e -> reveal());
        menu.add(revealItem);
        addUpdateItem();
        var quitItem = new MenuItem(MENU_QUIT);
        quitItem.addActionListener(e -> requestQuit());
        menu.add(quitItem);
        trayIcon.setPopupMenu(menu);
        trayIcon.setToolTip(UiConstants.APP_NAME);
    }

    /**
     * 刷新干活态菜单(Show/禁用状态行/更新检查项/Quit)与tooltip
     *
     * @param text 状态行与tooltip文案
     */
    private static void applyWorking(String text) {
        workingText = text;
        menu.removeAll();
        var showItem = new MenuItem(MENU_SHOW);
        showItem.addActionListener(e -> restoreWindow());
        menu.add(showItem);
        statusItem = new MenuItem(text);
        statusItem.setEnabled(false);
        menu.add(statusItem);
        addUpdateItem();
        var quitItem = new MenuItem(MENU_QUIT);
        quitItem.addActionListener(e -> requestQuit());
        menu.add(quitItem);
        trayIcon.setPopupMenu(menu);
        trayIcon.setToolTip(text);
    }

    /**
     * 渲染更新检查项插槽:位于Reveal last output/状态行之后、Quit之前,
     * 恰渲染一项,按更新状态切换文案与可用性
     */
    private static void addUpdateItem() {
        var handler = updateActionHandler;
        MenuItem item = null;
        switch (updateItemState) {
            case CHECK:
                item = new MenuItem(MENU_UPDATE_CHECK);
                item.addActionListener(e -> {
                    if (null != handler) {
                        handler.onCheckForUpdates();
                    }
                });
                break;
            case CHECKING:
                item = new MenuItem(MENU_UPDATE_CHECKING);
                item.setEnabled(false);
                break;
            case AVAILABLE:
                item = new MenuItem(String.format(MENU_UPDATE_AVAILABLE, null == updateVersion ? "" : updateVersion));
                item.addActionListener(e -> {
                    if (null != handler) {
                        handler.onUpdateAvailable();
                    }
                });
                break;
            case DOWNLOADING:
                item = new MenuItem(String.format(MENU_UPDATE_DOWNLOADING, null == updateVersion ? "" : updateVersion,
                        Math.max(updatePct, 0)));
                item.setEnabled(false);
                break;
            case DOWNLOADED:
                if (null == updateDmg || !updateDmg.exists()) {
                    // 缓存被手动删除:回退为已发现新版本且未下载,点击重新弹下载确认框
                    item = new MenuItem(
                            String.format(MENU_UPDATE_AVAILABLE, null == updateVersion ? "" : updateVersion));
                    item.addActionListener(e -> {
                        if (null != handler) {
                            handler.onUpdateAvailable();
                        }
                    });
                } else {
                    item = new MenuItem(MENU_UPDATE_OPEN);
                    item.addActionListener(e -> {
                        if (null != handler) {
                            handler.onOpenDownloaded();
                        }
                    });
                }
                break;
            default:
                return;
        }
        menu.add(item);
    }

    /**
     * 触发回到空闲态钩子,失败静默(不影响托盘主流程)
     */
    private static void runIdleHook() {
        var hook = idleHook;
        if (null == hook) {
            return;
        }
        try {
            hook.run();
        } catch (Throwable e) {
            // 空闲钩子异常不影响托盘
        }
    }

    /**
     * 组装干活态文案:done=已完成处理文件数(含失败),pct=按字节加权的整体进度
     *
     * @param progress 进度快照
     * @return "Encrypting {done}/{total} · {pct}%"
     */
    private static String buildProgressText(OperationProgressDTO progress) {
        var total = progress.getTotalFiles();
        var done = 0;
        var weightedPct = 0D;
        var weightSum = 0D;
        for (FileProgress fp : progress.getFileProgressList()) {
            var finished = FINISHED_STATE.equals(fp.getState());
            if (finished) {
                done++;
            }
            // 失败文件按100%计入(已处理完,不计剩余进度)
            var pct = finished && !SUCCESS_RESULT.equals(fp.getResult()) ? 100D : parsePercent(fp.getProgress());
            if (pct < 0) {
                pct = 0;
            }
            var size = parseSize(fp.getSourceFileSize());
            if (size < 0) {
                // 单个文件大小解析失败:按文件数均摊权重
                size = 1;
            }
            weightedPct += pct * size;
            weightSum += size;
        }
        var overallPct = weightSum > 0 ? (int) Math.round(weightedPct / weightSum) : 0;
        return String.format("Encrypting %d/%d · %d%%", done, total, overallPct);
    }

    /**
     * 解析百分比字符串(如"41%"),无法解析返回-1
     *
     * @param progress 百分比字符串
     * @return 百分比数值
     */
    private static double parsePercent(String progress) {
        if (progress == null) {
            return -1;
        }
        try {
            var text = progress.trim();
            if (text.endsWith("%")) {
                text = text.substring(0, text.length() - 1);
            }
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 解析人类可读大小字符串(如"1.24GB"/"512.00MB",与core Utils.capacityFormat同源),无法解析返回-1
     *
     * @param size 大小字符串
     * @return 字节数
     */
    private static double parseSize(String size) {
        if (size == null) {
            return -1;
        }
        try {
            var text = size.trim();
            var unit = 1D;
            if (text.endsWith("GB")) {
                text = text.substring(0, text.length() - 2);
                unit = 1024D * 1024 * 1024;
            } else if (text.endsWith("MB")) {
                text = text.substring(0, text.length() - 2);
                unit = 1024D * 1024;
            }
            return Double.parseDouble(text.trim()) * unit;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 组装完成通知摘要
     *
     * @param result 执行结果,为null表示异常路径
     * @return 摘要文案
     */
    private static String buildSummary(OperationResultDTO result) {
        if (result == null) {
            return "Operation failed";
        }
        return String.format("%d succeeded · %d failed", result.getSuccessCount(), result.getFailedCount());
    }

    /**
     * 注册macOS Cmd+Q拦截与Dock点击唤回(标准java.awt.desktop API)。
     * 各自独立降级:quit注册失败时Cmd+Q退回系统默认直接退出;
     * reopen注册失败时Dock点击不唤回,托盘Show与单例唤醒路径仍可用。
     */
    private static void registerMacCallbacks() {
        try {
            // Cmd+Q拦截:空闲直接退出,干活中走确认浮层
            Desktop.getDesktop().setQuitHandler(new QuitHandler() {
                @Override
                public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
                    response.cancelQuit();
                    requestQuit();
                }
            });
        } catch (Throwable e) {
            // quit回调注册失败:退回系统默认退出
        }
        try {
            // Dock点击唤回隐藏窗口
            Desktop.getDesktop().addAppEventListener(new AppReopenedListener() {
                @Override
                public void appReopened(AppReopenedEvent e) {
                    restoreWindow();
                }
            });
        } catch (Throwable e) {
            // reopen回调注册失败:托盘Show与单例唤醒路径仍可用
        }
    }
}
