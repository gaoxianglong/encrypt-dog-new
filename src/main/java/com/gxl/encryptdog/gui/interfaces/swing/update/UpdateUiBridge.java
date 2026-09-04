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

package com.gxl.encryptdog.gui.interfaces.swing.update;

import com.gxl.encryptdog.gui.infrastructure.update.UpdateChecker;
import com.gxl.encryptdog.gui.interfaces.swing.ThemedConfirmDialog;
import com.gxl.encryptdog.gui.interfaces.swing.tray.TrayManager;
import com.gxl.encryptdog.gui.interfaces.swing.tray.TrayManager.UpdateActionHandler;
import com.gxl.encryptdog.gui.interfaces.swing.tray.TrayManager.UpdateItemState;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Desktop;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 更新能力UI桥接:连接UpdateChecker(网络/状态机)与TrayManager(托盘插槽/通知)及确认弹窗。
 * 自动检查失败静默;手动检查结果始终反馈(有新版本弹窗/无新版本通知/失败通知)。
 * 自动检查发现新版本时若任务执行中则挂起,任务结束回到空闲态后补弹。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/4 15:00
 */
public final class UpdateUiBridge {
    /**
     * 确认弹窗标题
     */
    private static final String DIALOG_TITLE            = "EncryptDog Update";
    /**
     * 确认弹窗主按钮
     */
    private static final String DIALOG_DOWNLOAD         = "Download";
    /**
     * 确认弹窗次按钮
     */
    private static final String DIALOG_LATER            = "Later";
    /**
     * 手动检查无新版本弹窗文案
     */
    private static final String TEXT_UP_TO_DATE         = "Already up to date";
    /**
     * 手动检查失败通知
     */
    private static final String NOTIFY_CHECK_FAILED     = "Update check failed";
    /**
     * 下载失败通知
     */
    private static final String NOTIFY_DOWNLOAD_FAILED  = "Update download failed";
    /**
     * 下载完成通知模板
     */
    private static final String NOTIFY_DOWNLOADED       = "v%s downloaded. Drag EncryptDog into Applications to update.";
    /**
     * 确认弹窗副文案
     */
    private static final String DIALOG_SUB_MESSAGE      = "Download now, or check later from the menu bar";
    /**
     * 挂载备选路径hdiutil等待上限
     */
    private static final long   HDIUTIL_WAIT_S          = 30;

    /**
     * 主窗口(弹窗依附)
     */
    private static volatile JFrame  frame;
    /**
     * 最近发现的新版本号
     */
    private static volatile String  latestVersion;
    /**
     * 自动检查发现新版本但任务执行中,弹窗挂起待空闲补弹
     */
    private static volatile boolean pendingAutoPopup;

    private UpdateUiBridge() {
    }

    /**
     * 初始化桥接并启动一次自动检查:注册托盘点击回调与空闲钩子后发起检查。
     * 托盘未安装时仅托盘呈现缺失,弹窗/下载/挂载仍可用。
     *
     * @param owner 主窗口
     */
    public static void init(JFrame owner) {
        frame = owner;
        TrayManager.setUpdateActionHandler(new UpdateActionHandler() {
            @Override
            public void onCheckForUpdates() {
                checkManually();
            }

            @Override
            public void onUpdateAvailable() {
                showConfirmDialog();
            }

            @Override
            public void onOpenDownloaded() {
                openDownloaded();
            }
        });
        TrayManager.setIdleHook(UpdateUiBridge::drainPending);
        startAutoCheck();
    }

    /**
     * 启动自动检查(启动时一次):失败与无新版本均静默,有新版本弹窗(干活态挂起补弹)
     */
    private static void startAutoCheck() {
        TrayManager.setUpdateItemState(UpdateItemState.CHECKING, null, -1);
        UpdateChecker.startCheck(new UpdateChecker.CheckListener() {
            @Override
            public void onUpdateAvailable(String version, String dmgUrl) {
                latestVersion = version;
                SwingUtilities.invokeLater(() -> {
                    TrayManager.setUpdateItemState(UpdateItemState.AVAILABLE, version, -1);
                    if (TrayManager.isBusy()) {
                        // 干活态:弹窗挂起,任务结束回到空闲态后补弹
                        pendingAutoPopup = true;
                    } else {
                        showConfirmDialog();
                    }
                });
            }

            @Override
            public void onNoUpdate() {
                SwingUtilities.invokeLater(() -> TrayManager.setUpdateItemState(UpdateItemState.CHECK, null, -1));
            }

            @Override
            public void onFailure() {
                // 自动检查失败静默:插槽回到可检查态即可
                SwingUtilities.invokeLater(() -> TrayManager.setUpdateItemState(UpdateItemState.CHECK, null, -1));
            }
        });
    }

    /**
     * 托盘手动检查:结果始终即时反馈,检查或下载进行中重复触发被忽略
     */
    private static void checkManually() {
        if (UpdateChecker.isBusy()) {
            return;
        }
        TrayManager.setUpdateItemState(UpdateItemState.CHECKING, null, -1);
        UpdateChecker.startCheck(new UpdateChecker.CheckListener() {
            @Override
            public void onUpdateAvailable(String version, String dmgUrl) {
                latestVersion = version;
                SwingUtilities.invokeLater(() -> {
                    TrayManager.setUpdateItemState(UpdateItemState.AVAILABLE, version, -1);
                    // 手动检查用户在场,干活态也立即弹
                    showConfirmDialog();
                });
            }

            @Override
            public void onNoUpdate() {
                SwingUtilities.invokeLater(() -> {
                    TrayManager.setUpdateItemState(UpdateItemState.CHECK, null, -1);
                    showUpToDateDialog();
                });
            }

            @Override
            public void onFailure() {
                SwingUtilities.invokeLater(() -> {
                    TrayManager.setUpdateItemState(UpdateItemState.CHECK, null, -1);
                    TrayManager.notify(NOTIFY_CHECK_FAILED);
                });
            }
        });
    }

    /**
     * 弹"已是最新版本"提示框(EDT,手动检查专属):单按钮OK,窗口隐藏先恢复可见
     */
    private static void showUpToDateDialog() {
        SwingUtilities.invokeLater(() -> {
            if (null == frame) {
                return;
            }
            if (!frame.isVisible()) {
                TrayManager.restoreWindow();
            }
            ThemedConfirmDialog.showMessage(frame, DIALOG_TITLE, TEXT_UP_TO_DATE, "OK");
        });
    }

    /**
     * 弹更新确认框(EDT):窗口隐藏先恢复可见,选择Download进入下载
     */
    private static void showConfirmDialog() {
        SwingUtilities.invokeLater(() -> {
            var version = latestVersion;
            if (null == frame || null == version) {
                return;
            }
            if (!frame.isVisible()) {
                TrayManager.restoreWindow();
            }
            var confirmed = ThemedConfirmDialog.show(frame, DIALOG_TITLE,
                    String.format("New version %s is available", version), DIALOG_SUB_MESSAGE, DIALOG_DOWNLOAD,
                    DIALOG_LATER);
            if (confirmed) {
                startDownload();
            }
        });
    }

    /**
     * 启动下载:托盘显示进度,完成挂载打开并通知,失败通知并回到可检查态
     */
    private static void startDownload() {
        var version = latestVersion;
        TrayManager.setUpdateItemState(UpdateItemState.DOWNLOADING, version, 0);
        UpdateChecker.startDownload(new UpdateChecker.DownloadListener() {
            @Override
            public void onProgress(int pct) {
                SwingUtilities.invokeLater(
                        () -> TrayManager.setUpdateItemState(UpdateItemState.DOWNLOADING, version, pct));
            }

            @Override
            public void onDownloaded(File dmgFile, String downloadedVersion) {
                SwingUtilities.invokeLater(() -> {
                    TrayManager.setUpdateItemState(UpdateItemState.DOWNLOADED, downloadedVersion, -1, dmgFile);
                    TrayManager.notify(String.format(NOTIFY_DOWNLOADED, downloadedVersion));
                    openDmg(dmgFile);
                });
            }

            @Override
            public void onDownloadFailed() {
                SwingUtilities.invokeLater(() -> {
                    TrayManager.setUpdateItemState(UpdateItemState.CHECK, null, -1);
                    TrayManager.notify(NOTIFY_DOWNLOAD_FAILED);
                });
            }
        });
    }

    /**
     * 重新打开已下载的DMG(托盘Open downloaded update):缓存缺失时插槽回退为Update available
     * (仍已知存在新版本,点击重新弹下载确认框)
     */
    private static void openDownloaded() {
        SwingUtilities.invokeLater(() -> {
            var dmg = UpdateChecker.getDownloadedDmg();
            if (null == dmg || !dmg.exists()) {
                TrayManager.setUpdateItemState(UpdateItemState.AVAILABLE, latestVersion, -1);
                return;
            }
            openDmg(dmg);
        });
    }

    /**
     * 挂载并打开DMG:优先Desktop.open(等同双击),失败备选hdiutil attach后打开挂载点,再失败静默
     * (托盘Open downloaded update仍可重试)
     * @param dmg DMG文件
     */
    private static void openDmg(File dmg) {
        try {
            Desktop.getDesktop().open(dmg);
        } catch (Throwable e) {
            try {
                var process = new ProcessBuilder("hdiutil", "attach", dmg.getAbsolutePath()).start();
                process.waitFor(HDIUTIL_WAIT_S, TimeUnit.SECONDS);
                var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                var mountPoint = parseMountPoint(output);
                if (null != mountPoint) {
                    Desktop.getDesktop().open(new File(mountPoint));
                }
            } catch (Throwable e2) {
                // 挂载失败静默降级:缓存仍在,可经托盘重试
            }
        }
    }

    /**
     * 任务结束回到空闲态:补弹挂起的自动检查弹窗
     */
    private static void drainPending() {
        if (!pendingAutoPopup) {
            return;
        }
        pendingAutoPopup = false;
        if (null != latestVersion) {
            showConfirmDialog();
        }
    }

    /**
     * 从hdiutil attach输出解析挂载点:输出为TAB分列,取最后一行最后一个TAB列,
     * 以/Volumes/开头即挂载点(挂载点含空格时整列保留)
     * @param output hdiutil输出
     * @return 挂载点,解析失败返回null
     */
    static String parseMountPoint(String output) {
        if (null == output) {
            return null;
        }
        var lines = output.trim().split("\\R");
        if (0 == lines.length) {
            return null;
        }
        var columns = lines[lines.length - 1].split("\\t");
        var last = columns[columns.length - 1].trim();
        return last.startsWith("/Volumes/") ? last : null;
    }
}
