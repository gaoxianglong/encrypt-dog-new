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

package com.gxl.encryptdog;

import com.gxl.encryptdog.core.shell.EncryptDogConsole;
import com.gxl.encryptdog.gui.EncryptDogGui;
import com.gxl.encryptdog.gui.application.dto.EncryptFormDTO;
import com.gxl.encryptdog.gui.interfaces.swing.tray.TrayDaemon;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * 加密狗启动器
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/21 22:10
 */
public class Starter {
    /**
     * GUI模式参数
     */
    private static final String GUI_OPTION = "--gui";
     * 托盘守护模式参数(内部使用,由GUI启动时自动拉起)
    private static final String TRAY_OPTION = "--tray";

    public static void main(String[] args) {
        // 托盘守护模式路由:仅挂载菜单栏图标,不建窗口、不解析picocli
        if (Arrays.asList(args).contains(TRAY_OPTION)) {
            TrayDaemon.main(args);
        // GUI模式路由:剥离--gui参数后启动Swing图形界面
        var prefill = parseGuiArgs(args);
        if (Objects.nonNull(prefill)) {
            EncryptDogGui.launch(prefill);
            return;
        }
        // 终端模式:原picocli路径
        System.exit(new CommandLine(new EncryptDogConsole()).execute(args));
    }

    /**
     * 解析GUI模式参数,非GUI模式返回null
     * @param args
     * @return
     */
    private static EncryptFormDTO parseGuiArgs(String[] args) {
        var hasGui = false;
        for (var arg : args) {
            if (GUI_OPTION.equals(arg)) {
                hasGui = true;
                break;
            }
        }
        if (!hasGui) {
            return null;
        }
        // 解析剩余参数预填表单,密钥始终由用户在界面输入
        var form = new EncryptFormDTO();
        var files = new ArrayList<String>();
        for (var i = 0; i < args.length; i++) {
            var arg = args[i];
            if (GUI_OPTION.equals(arg) || TRAY_OPTION.equals(arg)) {
                continue;
            }
            if ("-s".equals(arg) || "--source-file".equals(arg)) {
                files.addAll(Arrays.asList(nextArg(args, ++i).split(",")));
                continue;
            }
            if ("-e".equals(arg) || "--encrypt".equals(arg)) {
                form.setEncrypt(true);
                continue;
            }
            if ("-a".equals(arg) || "--encrypt-algorithm".equals(arg)) {
                form.setEncryptAlgorithm(nextArg(args, ++i));
                continue;
            }
            if ("-d".equals(arg) || "--delete".equals(arg)) {
                form.setDelete(true);
                continue;
            }
            if ("-o".equals(arg) || "--only-local".equals(arg)) {
                form.setOnlyLocal(true);
                continue;
            }
            if ("-t".equals(arg) || "--target-path".equals(arg)) {
                form.setTargetPath(nextArg(args, ++i));
            }
        }
        if (!files.isEmpty()) {
            form.setSourceFilePaths(files);
        }
        return form;
    }

    /**
     * 获取指定索引处的参数值
     * @param args
     * @param index
     * @return
     */
    private static String nextArg(String[] args, int index) {
        return index < args.length ? args[index] : "";
    }
}
