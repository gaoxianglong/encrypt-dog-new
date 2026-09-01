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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 托盘双进程本地通信工具:仅绑定127.0.0.1,魔数握手区分本程序实例与陌生进程,
 * 命令为明文短行(show/quit),无敏感数据。同时承载守护进程的拉起与自身jar定位。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/1 14:00
 */
public final class TrayIpc {
    /**
     * 守护进程端口(存活检查+握手)
     */
    public static final int      DAEMON_PORT        = 32345;
    /**
     * GUI进程端口(单例锁+命令接收)
     */
    public static final int      GUI_PORT           = 32346;
    /**
     * 握手魔数(区分本程序实例与陌生进程)
     */
    private static final String  MAGIC              = "ENCRYPTDOG1";
    /**
     * 回环地址
     */
    private static final String  LOCALHOST          = "127.0.0.1";
    /**
     * 连接/读超时(毫秒)
     */
    private static final int     SOCKET_TIMEOUT_MS  = 2000;
    /**
     * 守护拉起后就绪轮询上限(毫秒)
     */
    private static final int     DAEMON_READY_MS    = 3000;
    /**
     * 就绪轮询间隔(毫秒)
     */
    private static final int     POLL_INTERVAL_MS   = 100;

    private TrayIpc() {
    }

    /**
     * 连接端口并完成魔数握手
     * @param port 目标端口
     * @return true=对端为本程序实例
     */
    public static boolean handshake(int port) {
        try (var socket = new Socket()) {
            socket.connect(new InetSocketAddress(LOCALHOST, port), SOCKET_TIMEOUT_MS);
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
            var out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out.println(MAGIC);
            return MAGIC.equals(in.readLine());
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 握手成功后发送命令
     * @param port 目标端口
     * @param command 短命令(show/quit)
     * @return true=发送成功
     */
    public static boolean sendCommand(int port, String command) {
        try (var socket = new Socket()) {
            socket.connect(new InetSocketAddress(LOCALHOST, port), SOCKET_TIMEOUT_MS);
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
            var out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out.println(MAGIC);
            if (!MAGIC.equals(in.readLine())) {
                return false;
            }
            out.println(command);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 服务侧握手:校验魔数并回应
     * @param socket 已接受的连接
     * @param out 输出流
     * @return 握手成功返回命令输入流(可继续读命令),陌生连接返回null
     * @throws IOException
     */
    public static BufferedReader handshakeServer(Socket socket, PrintWriter out) throws IOException {
        var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        if (!MAGIC.equals(in.readLine())) {
            return null;
        }
        out.println(MAGIC);
        return in;
    }

    /**
     * 绑定本机回环端口(单例锁)
     * @param port 端口
     * @return 绑定失败返回null
     */
    public static ServerSocket bindLocal(int port) {
        try {
            var server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(InetAddress.getByName(LOCALHOST), port));
            return server;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 确保守护进程运行:握手成功即已在;否则拉起并轮询就绪
     * @return true=守护已就绪
     */
    public static boolean ensureDaemon() {
        if (handshake(DAEMON_PORT)) {
            return true;
        }
        spawnDaemon();
        var deadline = System.currentTimeMillis() + DAEMON_READY_MS;
        while (System.currentTimeMillis() < deadline) {
            if (handshake(DAEMON_PORT)) {
                return true;
            }
            sleepQuietly(POLL_INTERVAL_MS);
        }
        return false;
    }

    /**
     * 拉起守护进程(java -Xmx64m -jar self --tray,独立进程不随GUI退出)
     */
    public static void spawnDaemon() {
        spawnJvm("-Xmx64m", "--tray");
    }

    /**
     * 拉起GUI进程(java -jar self --gui)
     */
    public static void spawnGui() {
        spawnJvm(null, "--gui");
    }

    /**
     * 以当前JVM拉起同jar的独立进程
     * @param vmArg JVM参数(可为null)
     * @param appArg 应用参数
     */
    private static void spawnJvm(String vmArg, String appArg) {
        var jar = selfJarPath();
        if (jar == null) {
            System.err.println("[tray] 无法定位自身jar,跳过进程拉起");
            return;
        }
        var javaBin = new File(System.getProperty("java.home"), "bin/java").getAbsolutePath();
        var cmd = new java.util.ArrayList<String>();
        cmd.add(javaBin);
        if (vmArg != null) {
            cmd.add(vmArg);
        }
        cmd.add("-jar");
        cmd.add(jar);
        cmd.add(appArg);
        try {
            // 输出落盘到临时目录日志,便于诊断拉起失败(仅守护/GUI进程自身异常信息,无敏感数据)
            var log = File.createTempFile("encrypt-dog-" + appArg.replace("--", "") + "-", ".log");
            new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log).start();
        } catch (IOException e) {
            System.err.println("[tray] 拉起进程失败: " + e.getMessage());
        }
    }

    /**
     * 定位自身jar路径(fat jar场景有效,IDE class目录场景返回null)
     * @return jar绝对路径或null
     */
    private static String selfJarPath() {
        try {
            var location = TrayIpc.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            var file = new File(location.toURI());
            if (!file.isFile() || !file.getName().endsWith(".jar")) {
                return null;
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 静默睡眠
     * @param millis 毫秒
     */
    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
