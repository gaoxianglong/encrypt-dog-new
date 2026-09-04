package com.gxl.encryptdog.gui.infrastructure.update;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * DMG下载链路单元测试(任务2.1):缓存目录落盘与进度回调、失败清理、
 * 同版本覆盖、下载期间重复触发守卫
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/4 15:00
 */
public class UpdateCheckerDownloadTest {
    /**
     * 回调事件类型
     */
    private static final int EVENT_DOWNLOADED = 0;
    private static final int EVENT_FAILED     = 1;
    /**
     * 服务端DMG字节数(200KB)
     */
    private static final int DMG_SIZE         = 204800;

    private HttpServer             server;
    private Path                   cacheDir;
    /**
     * 测试tag(自适应构建版本,严格高于当前版本)
     */
    private String                 tag;
    /**
     * release JSON中dmg资产指向的本地端点路径,测试可切换
     */
    private AtomicReference<String> dmgPath = new AtomicReference<>("/dmg");

    @Before
    public void setUp() throws Exception {
        UpdateChecker.resetForTest();
        tag = UpdateTestVersions.nextMajor();
        cacheDir = Files.createTempDirectory("encryptdog-update-test");
        UpdateChecker.overrideCacheDir(cacheDir.toFile());
        server = HttpServer.create(new InetSocketAddress(0), 0);
        // 检查接口:返回严格高于当前构建版本的tag,dmg资产地址指向dmgPath指向的端点
        server.createContext("/releases/latest", exchange -> {
            var json = String.format(
                    "{ \"tag_name\": \"v%s\", \"assets\": [ { \"name\": \"EncryptDog-%s.dmg\","
                            + " \"browser_download_url\": \"http://127.0.0.1:%d%s\" } ] }",
                    tag, tag, server.getAddress().getPort(), dmgPath.get());
            var bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        // 下载端点:返回DMG_SIZE个字节
        server.createContext("/dmg", exchange -> {
            exchange.sendResponseHeaders(200, DMG_SIZE);
            var buffer = new byte[16384];
            try (OutputStream output = exchange.getResponseBody()) {
                var remaining = DMG_SIZE;
                while (remaining > 0) {
                    var length = Math.min(buffer.length, remaining);
                    output.write(buffer, 0, length);
                    remaining -= length;
                }
            }
        });
        // 失败下载端点:500
        server.createContext("/dmg500", exchange -> exchange.sendResponseHeaders(500, -1));
        server.start();
        UpdateChecker.overrideApiUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/releases/latest");
    }

    @After
    public void tearDown() {
        server.stop(0);
        UpdateChecker.resetForTest();
    }

    /**
     * 完整下载:落盘完整、进度回调单调不减、缓存文件名正确
     */
    @Test
    public void testDownloadSuccess() throws Exception {
        var check = new LatchCheckListener();
        UpdateChecker.startCheck(check);
        assertTrue(check.latch.await(5, TimeUnit.SECONDS));
        assertEquals(0, check.event);

        var download = new LatchDownloadListener();
        UpdateChecker.startDownload(download);
        assertTrue(download.latch.await(10, TimeUnit.SECONDS));
        assertEquals(EVENT_DOWNLOADED, download.event);
        var dmg = download.file;
        assertNotNull(dmg);
        assertEquals("EncryptDog-" + tag + ".dmg", dmg.getName());
        assertEquals(DMG_SIZE, dmg.length());
        assertEquals(cacheDir.toFile(), dmg.getParentFile());
        // 进度回调:至少一次且百分比单调不减
        assertFalse(download.pcts.isEmpty());
        var previous = -1;
        for (var pct : download.pcts) {
            assertTrue("进度应单调不减: " + pct + " 前值 " + previous, pct >= previous);
            assertTrue(pct >= 0 && pct <= 100);
            previous = pct;
        }
        // 无.part残留
        try (var stream = Files.list(cacheDir)) {
            assertTrue(stream.noneMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
    }

    /**
     * 下载失败:onDownloadFailed且无半成品残留
     */
    @Test
    public void testDownloadFailure() throws Exception {
        dmgPath.set("/dmg500");
        var check = new LatchCheckListener();
        UpdateChecker.startCheck(check);
        assertTrue(check.latch.await(5, TimeUnit.SECONDS));
        assertEquals(0, check.event);

        var download = new LatchDownloadListener();
        UpdateChecker.startDownload(download);
        assertTrue(download.latch.await(10, TimeUnit.SECONDS));
        assertEquals(EVENT_FAILED, download.event);
        try (var stream = Files.list(cacheDir)) {
            assertTrue(stream.noneMatch(path -> path.getFileName().toString().startsWith("EncryptDog-")));
        }
    }

    /**
     * 同版本重复下载覆盖旧缓存
     */
    @Test
    public void testDownloadOverwrites() throws Exception {
        var stale = cacheDir.resolve("EncryptDog-" + tag + ".dmg");
        Files.write(stale, new byte[] { 1, 2, 3 });

        var check = new LatchCheckListener();
        UpdateChecker.startCheck(check);
        assertTrue(check.latch.await(5, TimeUnit.SECONDS));
        var download = new LatchDownloadListener();
        UpdateChecker.startDownload(download);
        assertTrue(download.latch.await(10, TimeUnit.SECONDS));
        assertEquals(EVENT_DOWNLOADED, download.event);
        assertEquals(DMG_SIZE, stale.toFile().length());
    }

    /**
     * 检查进行中触发下载被忽略
     */
    @Test
    public void testDownloadBusyGuard() throws Exception {
        var check = new LatchCheckListener();
        UpdateChecker.startCheck(check);
        var download = new LatchDownloadListener();
        UpdateChecker.startDownload(download);
        assertFalse("检查进行中下载应被忽略", download.latch.await(1, TimeUnit.SECONDS));
        assertTrue(check.latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * 锁存检查回调收集器
     */
    private static final class LatchCheckListener implements UpdateChecker.CheckListener {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile int         event = -1;

        @Override
        public void onUpdateAvailable(String version, String dmgUrl) {
            event = 0;
            latch.countDown();
        }

        @Override
        public void onNoUpdate() {
            event = 1;
            latch.countDown();
        }

        @Override
        public void onFailure() {
            event = 2;
            latch.countDown();
        }
    }

    /**
     * 锁存下载回调收集器
     */
    private static final class LatchDownloadListener implements UpdateChecker.DownloadListener {
        private final CountDownLatch              latch = new CountDownLatch(1);
        private final CopyOnWriteArrayList<Integer> pcts = new CopyOnWriteArrayList<>();
        private volatile int                      event = -1;
        private volatile File                     file;

        @Override
        public void onProgress(int pct) {
            pcts.add(pct);
        }

        @Override
        public void onDownloaded(File dmgFile, String version) {
            file = dmgFile;
            event = EVENT_DOWNLOADED;
            latch.countDown();
        }

        @Override
        public void onDownloadFailed() {
            event = EVENT_FAILED;
            latch.countDown();
        }
    }
}
