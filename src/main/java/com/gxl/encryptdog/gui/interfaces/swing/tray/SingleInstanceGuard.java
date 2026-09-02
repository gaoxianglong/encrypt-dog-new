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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * GUI单例锁:localhost端口占用判定唯一实例。首个实例bind成功并监听show命令;
 * 后续实例bind失败则握手唤醒已有实例后自行退出;陌生进程占用端口时降级正常启动。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/2 15:00
 */
public class SingleInstanceGuard {
    /**
     * 单例端口(仅绑定127.0.0.1)
     */
    private static final int    SINGLETON_PORT = 32346;
    /**
     * 握手魔数
     */
    private static final String MAGIC          = "ENCRYPTDOG1";
    /**
     * 唤醒命令
     */
    private static final String CMD_SHOW       = "show";
    /**
     * 唤醒确认回复
     */
    private static final String REPLY_OK       = "ok";
    /**
     * 握手IO超时
     */
    private static final int    IO_TIMEOUT_MS  = 2000;
    /**
     * 日志
     */
    private static final Logger LOG            = LoggerFactory.getLogger(SingleInstanceGuard.class);

    /**
     * 唤醒回调(窗口创建后由EncryptDogGui注入)
     */
    private static volatile Runnable wakeAction = () -> {
    };

    private SingleInstanceGuard() {
    }

    /**
     * 确保单例:窗口创建前调用。首个实例bind成功并起监听线程;
     * 端口被占用时尝试握手唤醒已有实例并退出;陌生占用降级正常启动。
     */
    public static void ensureSingle() {
        try {
            var server = new ServerSocket(SINGLETON_PORT, 50, InetAddress.getByName("127.0.0.1"));
            startAcceptThread(server);
        } catch (IOException e) {
            // 端口占用:可能是已有实例(唤醒之)或陌生进程(降级正常启动)
            tryWakeAndExit();
        }
    }

    /**
     * 注入唤醒回调(窗口创建后)
     *
     * @param action 唤醒动作
     */
    public static void setWakeAction(Runnable action) {
        wakeAction = action;
    }

    /**
     * 起监听线程:魔数握手通过后接收show命令,回复ok并在EDT上执行唤醒回调
     *
     * @param server 单例监听socket
     */
    private static void startAcceptThread(ServerSocket server) {
        var thread = new Thread(() -> {
            while (true) {
                try (var socket = server.accept()) {
                    socket.setSoTimeout(IO_TIMEOUT_MS);
                    var reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    var writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                    if (!MAGIC.equals(reader.readLine())) {
                        continue;
                    }
                    if (CMD_SHOW.equals(reader.readLine())) {
                        writer.write(REPLY_OK + "\n");
                        writer.flush();
                        SwingUtilities.invokeLater(wakeAction);
                    }
                } catch (IOException e) {
                    // 单连接异常不影响监听循环
                }
            }
        }, "encryptdog-singleton");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 尝试握手唤醒已有实例:魔数+show命令换得ok回复则本进程退出;
     * 连接失败/超时/魔数不符视为陌生进程占用,降级正常启动并记日志。
     */
    private static void tryWakeAndExit() {
        try (var socket = new Socket(InetAddress.getByName("127.0.0.1"), SINGLETON_PORT)) {
            socket.setSoTimeout(IO_TIMEOUT_MS);
            var writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            writer.write(MAGIC + "\n" + CMD_SHOW + "\n");
            writer.flush();
            var reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            if (REPLY_OK.equals(reader.readLine())) {
                System.exit(0);
            }
            LOG.warn("singleton port {} occupied by an unknown process, starting normally without single-instance protection",
                    SINGLETON_PORT);
        } catch (IOException e) {
            LOG.warn("singleton port {} occupied by an unknown process, starting normally without single-instance protection",
                    SINGLETON_PORT);
        }
    }
}
