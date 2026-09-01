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
import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;
import com.gxl.encryptdog.gui.interfaces.swing.tray.TrayIpc;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * GUI启动器:安装FlatLaf暗色主题并打开主窗口
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class EncryptDogGui {
     * 主窗口引用(单例锁命令接收线程使用)
     * GUI端口ServerSocket(同时充当单例锁)
    private static ServerSocket           guiServer;
    private EncryptDogGui() {
    }

    /**
     * 启动GUI
     * @param prefill 预填表单数据,--gui后剩余命令行参数解析而来,可为null
     */
    public static void launch(EncryptFormDTO prefill) {
        // 单例锁:GUI端口已被占用时,若为本程序旧实例则唤醒后退出,陌生进程占用则降级正常启动(单例保护失效)
        if (!acquireSingleton()) {
            if (TrayIpc.handshake(TrayIpc.GUI_PORT)) {
                TrayIpc.sendCommand(TrayIpc.GUI_PORT, "show");
                System.exit(0);
        TrayIpc.ensureDaemon();
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
                frame.setVisible(true);
            }
        });
    }

     * 绑定GUI端口作为单例锁,并启动命令接收线程
    private static boolean acquireSingleton() {
        guiServer = TrayIpc.bindLocal(TrayIpc.GUI_PORT);
        if (guiServer == null) {
        var thread = new Thread(EncryptDogGui::serveCommands, "encrypt-dog-gui-ipc");
        thread.setDaemon(true);
     * GUI端口命令接收循环:show=带前台,quit=走标准关闭路径(EXIT_ON_CLOSE)
    private static void serveCommands() {
        while (!guiServer.isClosed()) {
            try (Socket socket = guiServer.accept()) {
                socket.setSoTimeout(2000);
                var out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                var in = TrayIpc.handshakeServer(socket, out);
                        var f = frame;
                            f.setState(Frame.NORMAL);
                            f.setVisible(true);
                            f.toFront();
                            f.requestFocus();
                        var f = frame;
                            f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
    public static void main(String[] args) {
        launch(null);
    }
}
