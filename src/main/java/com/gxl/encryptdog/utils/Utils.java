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

package com.gxl.encryptdog.utils;

import com.google.common.base.Charsets;
import com.gxl.encryptdog.base.common.Constants;
import com.taobao.text.util.RenderUtil;
import lombok.Getter;
import lombok.ToString;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * 工具类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/21 23:32
 */
public class Utils {
    /**
     * 验证是否是macos
     * @return
     */
    public static boolean isMacOperatingSystem() {
        return Utils.getOperatingSystemName().toLowerCase().startsWith("mac");
    }

    /**
     * 获取操作系统名称
     * @return
     */
    public static String getOperatingSystemName() {
        return System.getProperty("os.name");
    }

    /**
     * 获取操作类型
     * @param isEncrypt
     * @return
     */
    public static String getOperateType(boolean isEncrypt) {
        return isEncrypt ? "ENCRYPT" : "DECRYPT";
    }

    /**
     * 获取目标资源的InputStream
     * @param name
     * @return
     */
    public static InputStream getResourceStream(String name) {
        return Utils.class.getClassLoader().getResourceAsStream(name);
    }

    /**
     * 判断是否是控制台运行
     * @return
     */
    public static boolean isConsole() {
        var console = System.console();
        return Objects.nonNull(console);
    }

    /**
     * 根据全限定名解析文件名称和目录
     * @param path
     * @return
     */
    public static FileStructure parseFileStructure(String path) {
        if (path.isBlank()) {
            return null;
        }
        var file = new File(path);
        return new FileStructure(file.getName(), file.getParent());
    }

    /**
     * 文件信息
     */
    @ToString
    @Getter
    public static class FileStructure implements Serializable {
        @Serial
        private static final long serialVersionUID = -1159868079589975157L;
        /**
         * 文件名称
         */
        private String            fileName;
        /**
         * 文件目录
         */
        private String            fileDir;

        public FileStructure(String fileName, String fileDir) {
            this.fileName = fileName;
            this.fileDir = fileDir;
        }
    }

    /**
     * 解析文件后缀
     * @param fileName
     * @return
     */
    public static String parseFileSuffix(String fileName) {
        if (fileName.isBlank()) {
            return null;
        }
        try {
            return fileName.substring(fileName.lastIndexOf(Constants.PERIOD_SEPARATOR));
        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * 控制台输入
     * @return
     */
    public static String consoleInput() {
        return new Scanner(System.in).nextLine();
    }

    /**
     * 清屏命令
     * @return
     */
    public static String cls() {
        return RenderUtil.cls();
    }

    /**
     * 容量单位转换
     *
     * @param size
     * @return
     */
    public static String capacityFormat(long size) {
        if (size >= Constants.GB) {
            return String.format("%.2fGB", (double) size / Constants.GB);
        }
        return String.format("%.2fMB", (double) size / Constants.MB);
    }

    /**
     * 限制字符串的最大长度,如果超过则裁剪
     * @param str
     * @param maxLength
     * @return
     */
    public static String lengthShear(String str, int maxLength) {
        var size = str.length();
        if (size <= maxLength) {
            return str;
        }
        return String.format("...%s", str.substring(size - maxLength, size));
    }

    /**
     * 计算目标文件的容量
     * @param filePath
     * @return
     */
    public static long getFileCapacity(String filePath) {
        var file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.length();
        }
        return -1L;
    }

    /**
     * 整型转字节数组
     *
     * @param n
     * @return
     */
    public static byte[] int2Bytes(int n) {
        var result = new byte[4];
        result[3] = (byte) (n & 0xff);
        result[2] = (byte) (n >> 8 & 0xff);
        result[1] = (byte) (n >> 16 & 0xff);
        result[0] = (byte) (n >> 24 & 0xff);
        return result;
    }

    /**
     * 字节数组转整型
     *
     * @param b
     * @return
     */
    public static int bytes2Int(byte[] b) {
        Objects.requireNonNull(b);
        var result = 0;
        for (var i = 0; i < b.length; i++) {
            result += (b[i] & 0xff) << ((3 - i) * 8);
        }
        return result;
    }

    /**
     * base64加密
     *
     * @param s
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toBase64Encode(byte[] s) throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString(s);
    }

    /**
     * long转字节数组
     *
     * @param x
     * @return
     */
    public static byte[] long2Bytes(long x) {
        var buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }

    /**
     * 目标文件创建
     * @param filePath
     * @throws IOException
     */
    public static void createTargetFile(String filePath) throws IOException {
        // 创建目标文件夹
        createTargetDir(parseFileStructure(filePath).getFileDir());
        var targetFile = new File(filePath);
        if (!targetFile.exists()) {
            // 创建目标文件
            targetFile.createNewFile();
        }
    }

    /**
     * 创建目标文件夹
     * @param targetDir
     */
    public static void createTargetDir(String targetDir) {
        var targetFileDir = new File(targetDir);
        if (!targetFileDir.exists()) {
            // 创建目标文件夹
            targetFileDir.mkdirs();
        }
    }

    /**
     * 字符转字节
     *
     * @param chars
     * @return
     */
    public static byte[] chars2Bytes(char[] chars) {
        var cs = Charset.forName(Charsets.UTF_8.name());
        var cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        var bb = cs.encode(cb);
        return bb.array();
    }

    /**
     * 字节转字符
     *
     * @param bytes
     * @return
     */
    public static char[] bytes2Chars(byte[] bytes) {
        var cs = Charset.forName(Charsets.UTF_8.name());
        var bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes).flip();
        var cb = cs.decode(bb);
        return cb.array();
    }

    /**
     * 将时间戳转换为指定的时间格式 hh:mm:ss
     *
     * @param currentTime
     * @return
     */
    public static String currentTimeFormat(long currentTime) {
        var rlt = new StringJoiner(Constants.COLON);
        var hour = currentTime / Constants.HOUR;
        currentTime -= hour * Constants.HOUR;
        var min = currentTime / Constants.MINUTE;
        currentTime -= min * Constants.MINUTE;
        rlt.add(String.format("%02d", hour)).add(String.format("%02d", min)).add(String.format("%02d", currentTime));
        return rlt.toString();
    }

    /**
     * base64解密
     *
     * @param s
     * @return
     */
    public static byte[] toBase64Decode(byte[] s) {
        return Base64.getDecoder().decode(s);
    }

    /**
     * 字节数组转long
     *
     * @param bytes
     * @return
     */
    public static long bytes2Long(byte[] bytes) {
        var buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * 删除文件
     * @param path
     */
    public static void deleteFile(String path) {
        var sourceFile = new File(path);
        if (sourceFile.exists()) {
            sourceFile.delete();
        }
    }
}
