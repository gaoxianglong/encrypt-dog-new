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

import com.gxl.encryptdog.gui.interfaces.swing.LogoUtil;
import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;

import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 托盘守护进程:仅挂载菜单栏logo图标(无窗口、无渲染动画、无定时器),
 * Open唤醒/拉起GUI,Quit通知GUI关闭后全退。守护端口绑定构成进程单例。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/1 14:00
 */
public final class TrayDaemon {
    /**
     * 托盘图标显示尺寸(pt)
     */
    private static final int ICON_SIZE   = 22;
    /**
     * 托盘图标2x高清变体(px)
     */
    private static final int ICON_SIZE_2X = 44;
    /**
     * 工具提示
     */
    private static final String TOOLTIP   = "Encrypt Dog";
    /**
     * 托盘菜单项文案
     */
    private static final String MENU_OPEN = "Open Encrypt Dog";
    /**
     * 托盘菜单项文案
     */
    private static final String MENU_QUIT = "Quit";
    /**
     * 当前托盘图标(Quit时移除)
     */
    private static TrayIcon     icon;

    private TrayDaemon() {
    }

    /**
     * 守护入口(--tray路由)
     * @param args 忽略
     */
    public static void main(String[] args) {
        if (!SystemTray.isSupported()) {
            // 无托盘环境守护无意义,直接退出
            System.exit(0);
        }
        // 守护端口绑定=进程单例:失败说明已有守护或陌生占用,直接退出(GUI侧握手判定归属)
        var server = TrayIpc.bindLocal(TrayIpc.DAEMON_PORT);
        if (server == null) {
            System.exit(0);
        }
        // 接受线程保持非daemon:JVM不随main返回而退出,守护常驻
        var acceptThread = new Thread(() -> acceptLoop(server), "encrypt-dog-tray-accept");
        acceptThread.setDaemon(false);
        acceptThread.start();
        try {
            // 托盘图标必须在EDT创建
            SwingUtilities.invokeAndWait(TrayDaemon::installTrayIcon);
        } catch (Exception e) {
            System.exit(0);
        }
    }

    /**
     * 守护端口接受循环:魔数握手后关闭,服务GUI侧存活检查
     * @param server 守护ServerSocket
     */
    private static void acceptLoop(ServerSocket server) {
        while (!server.isClosed()) {
            try (Socket socket = server.accept()) {
                socket.setSoTimeout(2000);
                var out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                var in = TrayIpc.handshakeServer(socket, out);
                if (in != null) {
                    // 握手通过即证明守护存活,无需读取命令
                    in.readLine();
                }
            } catch (IOException e) {
                // 连接异常/超时直接忽略,继续接受
            }
        }
    }

    /**
     * 安装托盘图标与菜单(EDT)
     */
    private static void installTrayIcon() {
        var icon22 = LogoUtil.loadImage(UiConstants.LOGO_RESOURCE, ICON_SIZE);
        var icon44 = LogoUtil.loadImage(UiConstants.LOGO_RESOURCE, ICON_SIZE_2X);
        if (icon22 == null || icon44 == null) {
            // logo资源缺失守护无意义
            System.exit(0);
        }
        var menu = new PopupMenu();
        var openItem = new MenuItem(MENU_OPEN);
        openItem.addActionListener(e -> openGui());
        var quitItem = new MenuItem(MENU_QUIT);
        quitItem.addActionListener(e -> quitAll());
        menu.add(openItem);
        menu.add(quitItem);
        // 22pt + 44px@2x双档,关闭自动缩放防retina模糊
        var image = new BaseMultiResolutionImage(icon22.getImage(), icon44.getImage());
        icon = new TrayIcon(image, TOOLTIP, menu);
        icon.setImageAutoSize(false);
        try {
            SystemTray.getSystemTray().add(icon);
        } catch (AWTException e) {
            System.exit(0);
        }
    }

    /**
     * 托盘Open:GUI已运行则带前台,未运行则拉起新GUI进程
     */
    private static void openGui() {
        if (!TrayIpc.sendCommand(TrayIpc.GUI_PORT, "show")) {
            TrayIpc.spawnGui();
        }
    }

    /**
     * 托盘Quit:通知GUI关闭(未运行则忽略),移除图标,守护退出
     */
    private static void quitAll() {
        TrayIpc.sendCommand(TrayIpc.GUI_PORT, "quit");
        if (icon != null) {
            SystemTray.getSystemTray().remove(icon);
        }
        System.exit(0);
    }
}
