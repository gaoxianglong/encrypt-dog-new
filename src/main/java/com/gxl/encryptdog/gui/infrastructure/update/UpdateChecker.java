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

package com.gxl.encryptdog.gui.infrastructure.update;

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 更新检查基础设施:GitHub latest release查询、版本与.dmg资产判定、DMG下载。
 * 纯网络与状态机逻辑,不含Swing依赖;UI呈现(托盘插槽/弹窗/通知)由展示层桥接完成。
 * 状态机:空闲 -> 检查中 -> 有新版本/无新版本 -> 下载中 -> 已下载/失败,
 * 单工作线程串行推进,检查或下载进行中的重复触发被忽略。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/4 15:00
 */
public final class UpdateChecker {
    /**
     * GitHub latest release查询地址(可通过系统属性覆盖,用于本地mock验收)
     */
    private static final String API_URL_PROPERTY    = "encryptdog.update.apiUrl";
    /**
     * DMG缓存目录(可通过系统属性覆盖,用于本地mock验收)
     */
    private static final String CACHE_DIR_PROPERTY  = "encryptdog.update.cacheDir";
    /**
     * 缺省查询地址
     */
    private static final String DEFAULT_API_URL     = "https://api.github.com/repos/gaoxianglong/encrypt-dog-new/releases/latest";
    /**
     * 缺省缓存目录:~/Library/Caches/EncryptDog/update
     */
    private static final String DEFAULT_CACHE_DIR   = String.join(File.separator, System.getProperty("user.home"),
            "Library", "Caches", "EncryptDog", "update");
    /**
     * 查询请求读取超时
     */
    private static final long   REQUEST_TIMEOUT_S   = 5;
    /**
     * 下载请求读取超时(大文件慢网宽松上限,连接超时仍为3s)
     */
    private static final long   DOWNLOAD_TIMEOUT_M  = 30;
    /**
     * 进度回调节流间隔(毫秒)
     */
    private static final long   PROGRESS_THROTTLE   = 500;
    /**
     * 下载缓冲大小
     */
    private static final int    DOWNLOAD_BUFFER     = 65536;
    /**
     * tag_name提取
     */
    private static final Pattern TAG_PATTERN        = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    /**
     * assets数组提取(资产对象无嵌套数组,非贪婪匹配到首个右括号即数组结束)
     */
    private static final Pattern ASSETS_PATTERN     = Pattern.compile("\"assets\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
    /**
     * 资产名提取
     */
    private static final Pattern ASSET_NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    /**
     * 资产下载地址提取
     */
    private static final Pattern ASSET_URL_PATTERN  = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"");

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateChecker.class);

    /**
     * 检查结果回调(工作线程触发,展示层负责切EDT)
     */
    public interface CheckListener {
        /**
         * 发现新版本
         * @param version 新版本号(已归一化)
         * @param dmgUrl dmg资产下载地址
         */
        void onUpdateAvailable(String version, String dmgUrl);

        /**
         * 无新版本(版本相同/无dmg资产)
         */
        void onNoUpdate();

        /**
         * 检查失败(网络异常/接口错误/解析失败)
         */
        void onFailure();
    }

    /**
     * 下载结果回调(工作线程触发,展示层负责切EDT)
     */
    public interface DownloadListener {
        /**
         * 下载进度(节流约500ms)
         * @param pct 0~100,无法获取总大小时为-1
         */
        void onProgress(int pct);

        /**
         * 下载完成
         * @param dmgFile 缓存DMG文件
         * @param version 版本号
         */
        void onDownloaded(File dmgFile, String version);

        /**
         * 下载失败
         */
        void onDownloadFailed();
    }

    /**
     * 工作线程(单守护线程串行执行检查与下载)
     */
    private static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), runnable -> {
                var thread = new Thread(runnable, "encryptdog-update");
                thread.setDaemon(true);
                return thread;
            });
    /**
     * HTTP客户端(跟随重定向:GitHub资产下载地址302跳转)
     */
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    /**
     * 状态机:检查或下载进行中
     */
    private static volatile boolean busy;
    /**
     * 当前查询地址(测试可覆盖)
     */
    private static volatile String  apiUrl        = resolveApiUrl();
    /**
     * 当前缓存目录(测试可覆盖)
     */
    private static volatile File    cacheDir      = new File(resolveCacheDir());
    /**
     * 最新发现的版本号
     */
    private static volatile String  latestVersion;
    /**
     * 最新dmg资产下载地址
     */
    private static volatile String  pendingDmgUrl;
    /**
     * 已下载的DMG缓存文件
     */
    private static volatile File    downloadedDmg;

    private UpdateChecker() {
    }

    /**
     * 是否处于检查或下载进行中
     * @return true=忙碌(重复触发应被忽略)
     */
    public static boolean isBusy() {
        return busy;
    }

    /**
     * 读取当前运行时版本(dog.properties的project.version),读取失败返回null
     * @return 当前版本,读取失败返回null
     */
    public static String currentVersion() {
        try (var input = Utils.getResourceStream(Constants.DEFAULT_CONFIG_PATH)) {
            var properties = new Properties();
            properties.load(input);
            return properties.getProperty("project.version");
        } catch (Throwable e) {
            LOGGER.warn("Failed to read current version from dog.properties", e);
            return null;
        }
    }

    /**
     * 触发一次版本检查,检查或下载进行中时忽略本次请求
     * @param listener 结果回调
     */
    public static void startCheck(CheckListener listener) {
        if (busy) {
            return;
        }
        busy = true;
        WORKER.execute(() -> {
            try {
                var latest = fetchLatest();
                if (null == latest) {
                    listener.onFailure();
                    return;
                }
                if (null == latest.version) {
                    // tag_name缺失:响应不可解析,按检查失败处理
                    listener.onFailure();
                    return;
                }
                var current = currentVersion();
                if (null == current || !VersionComparator.isNewer(current, latest.version)) {
                    listener.onNoUpdate();
                    return;
                }
                if (null == latest.dmgUrl) {
                    listener.onNoUpdate();
                    return;
                }
                var normalized = VersionComparator.normalize(latest.version);
                latestVersion = normalized;
                pendingDmgUrl = latest.dmgUrl;
                listener.onUpdateAvailable(normalized, latest.dmgUrl);
            } catch (Throwable e) {
                LOGGER.warn("Update check failed", e);
                listener.onFailure();
            } finally {
                busy = false;
            }
        });
    }

    /**
     * 触发dmg下载(需先完成一次发现新版本的检查),检查或下载进行中时忽略本次请求
     * @param listener 下载回调
     */
    public static void startDownload(DownloadListener listener) {
        if (busy) {
            return;
        }
        var url = pendingDmgUrl;
        var version = latestVersion;
        if (null == url || null == version) {
            return;
        }
        busy = true;
        WORKER.execute(() -> {
            var part = (File) null;
            try {
                var dir = cacheDir;
                if (!dir.exists() && !dir.mkdirs()) {
                    listener.onDownloadFailed();
                    return;
                }
                var file = new File(dir, String.format("EncryptDog-%s.dmg", version));
                part = new File(dir, file.getName() + ".part");
                var request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(DOWNLOAD_TIMEOUT_M)).GET()
                        .build();
                var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (200 != response.statusCode()) {
                    listener.onDownloadFailed();
                    return;
                }
                var total = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
                var done = 0L;
                var lastReport = 0L;
                try (var input = response.body();
                        var output = new BufferedOutputStream(new FileOutputStream(part))) {
                    var buffer = new byte[DOWNLOAD_BUFFER];
                    int length;
                    while (-1 != (length = input.read(buffer))) {
                        output.write(buffer, 0, length);
                        done += length;
                        var now = System.currentTimeMillis();
                        if (now - lastReport >= PROGRESS_THROTTLE) {
                            lastReport = now;
                            listener.onProgress(total > 0 ? (int) (done * 100 / total) : -1);
                        }
                    }
                }
                Files.move(part.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                downloadedDmg = file;
                listener.onDownloaded(file, version);
            } catch (Throwable e) {
                LOGGER.warn("Update download failed", e);
                if (null != part) {
                    part.delete();
                }
                listener.onDownloadFailed();
            } finally {
                busy = false;
            }
        });
    }

    /**
     * 获取已下载的DMG缓存文件
     * @return 缓存文件,未下载过返回null
     */
    public static File getDownloadedDmg() {
        return downloadedDmg;
    }

    /**
     * 查询并解析latest release,HTTP失败返回null
     * @return 版本与dmg地址,HTTP失败返回null
     */
    private static LatestInfo fetchLatest() {
        try {
            var request = HttpRequest.newBuilder(URI.create(apiUrl)).timeout(Duration.ofSeconds(REQUEST_TIMEOUT_S))
                    .header("Accept", "application/vnd.github+json").header("User-Agent", "EncryptDog")
                    .GET().build();
            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (200 != response.statusCode()) {
                return null;
            }
            var body = response.body();
            return new LatestInfo(match(TAG_PATTERN, body), findDmgAssetUrl(body));
        } catch (Throwable e) {
            LOGGER.warn("Failed to fetch latest release", e);
            return null;
        }
    }

    /**
     * 从assets数组中挑选dmg资产:name以EncryptDog开头且以.dmg结尾,取首个匹配项的下载地址
     * @param body 响应JSON
     * @return 下载地址,无匹配返回null
     */
    private static String findDmgAssetUrl(String body) {
        var assets = match(ASSETS_PATTERN, body);
        if (null == assets) {
            return null;
        }
        for (var chunk : assets.split("\\}\\s*,\\s*\\{")) {
            var name = match(ASSET_NAME_PATTERN, chunk);
            var url = match(ASSET_URL_PATTERN, chunk);
            if (null != name && null != url && name.startsWith("EncryptDog") && name.endsWith(".dmg")) {
                return url;
            }
        }
        return null;
    }

    /**
     * 正则提取首个匹配组,无匹配返回null
     * @param pattern 模式
     * @param input 输入
     * @return 匹配组,无匹配返回null
     */
    private static String match(Pattern pattern, String input) {
        var matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 解析系统属性覆盖的查询地址
     * @return 查询地址
     */
    private static String resolveApiUrl() {
        var override = System.getProperty(API_URL_PROPERTY);
        return null == override || override.isEmpty() ? DEFAULT_API_URL : override;
    }

    /**
     * 解析系统属性覆盖的缓存目录
     * @return 缓存目录路径
     */
    private static String resolveCacheDir() {
        var override = System.getProperty(CACHE_DIR_PROPERTY);
        return null == override || override.isEmpty() ? DEFAULT_CACHE_DIR : override;
    }

    /**
     * 最新release解析结果
     */
    private static final class LatestInfo {
        /**
         * 版本号(未归一化)
         */
        private final String version;
        /**
         * dmg资产下载地址,无匹配为null
         */
        private final String dmgUrl;

        private LatestInfo(String version, String dmgUrl) {
            this.version = version;
            this.dmgUrl = dmgUrl;
        }
    }

    /**
     * 测试钩子:覆盖查询地址
     * @param url 查询地址
     */
    static void overrideApiUrl(String url) {
        apiUrl = url;
    }

    /**
     * 测试钩子:覆盖缓存目录
     * @param dir 缓存目录
     */
    static void overrideCacheDir(File dir) {
        cacheDir = dir;
    }

    /**
     * 测试钩子:复位共享状态(工作线程由JVM级复用,仅复位状态字段)
     */
    static void resetForTest() {
        busy = false;
        latestVersion = null;
        pendingDmgUrl = null;
        downloadedDmg = null;
        apiUrl = resolveApiUrl();
        cacheDir = new File(resolveCacheDir());
    }
}
