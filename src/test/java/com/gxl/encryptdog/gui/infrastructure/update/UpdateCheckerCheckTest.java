package com.gxl.encryptdog.gui.infrastructure.update;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 版本检查链路单元测试(任务1.2):本地mock HTTP服务验证JSON解析、
 * 404/403/网络异常/解析失败均静默失败、无dmg资产与同版本视为无新版本、忙碌守卫
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/4 15:00
 */
public class UpdateCheckerCheckTest {
    /**
     * 回调事件类型
     */
    private static final int EVENT_AVAILABLE = 0;
    private static final int EVENT_NO_UPDATE = 1;
    private static final int EVENT_FAILURE   = 2;

    private HttpServer               server;
    private AtomicInteger            status = new AtomicInteger(200);
    private AtomicReference<String>  body   = new AtomicReference<>("{}");
    private AtomicBoolean            slow   = new AtomicBoolean(false);

    @Before
    public void setUp() throws Exception {
        UpdateChecker.resetForTest();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/releases/latest", exchange -> {
            if (slow.get()) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            var bytes = body.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
        UpdateChecker.overrideApiUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/releases/latest");
    }

    @After
    public void tearDown() {
        server.stop(0);
        UpdateChecker.resetForTest();
    }

    /**
     * 更高版本且有dmg资产判定为新版本(tag自适应构建版本,严格高于当前版本)
     */
    @Test
    public void testNewerVersionFound() throws Exception {
        var tag = UpdateTestVersions.nextMajor();
        body.set(releaseJson("v" + tag, "EncryptDog-2.1.0.dmg", "http://127.0.0.1/dmg/EncryptDog-2.1.0.dmg"));
        var listener = new LatchCheckListener();
        UpdateChecker.startCheck(listener);
        assertTrue("回调未在超时内触发", listener.latch.await(5, TimeUnit.SECONDS));
        assertEquals(EVENT_AVAILABLE, listener.event);
        assertEquals(tag, listener.version);
        assertEquals("http://127.0.0.1/dmg/EncryptDog-2.1.0.dmg", listener.url);
    }

    /**
     * 与当前构建版本相同的tag不判定为新版本(pom版本如何调整均成立)
     */
    @Test
    public void testSameVersionNoUpdate() throws Exception {
        var current = UpdateChecker.currentVersion();
        body.set(releaseJson("v" + current, "EncryptDog-2.0.5.dmg", "http://127.0.0.1/dmg/EncryptDog-2.0.5.dmg"));
        var listener = new LatchCheckListener();
        UpdateChecker.startCheck(listener);
        assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        assertEquals(EVENT_NO_UPDATE, listener.event);
    }

    /**
     * 无dmg资产视为无新版本
     */
    @Test
    public void testNoDmgAsset() throws Exception {
        body.set(releaseJson("v2.1.0", "encryptdog-2.1.0.jar", "http://127.0.0.1/encryptdog-2.1.0.jar"));
        var listener = new LatchCheckListener();
        UpdateChecker.startCheck(listener);
        assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        assertEquals(EVENT_NO_UPDATE, listener.event);
    }

    /**
     * 404静默失败
     */
    @Test
    public void testHttp404() throws Exception {
        status.set(404);
        var listener = new LatchCheckListener();
        UpdateChecker.startCheck(listener);
        assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        assertEquals(EVENT_FAILURE, listener.event);
    }

    /**
     * 403(限流)静默失败
     */
    @Test
    public void testHttp403() throws Exception {
        status.set(403);
        var listener = new LatchCheckListener();
        UpdateChecker.startCheck(listener);
        assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        assertEquals(EVENT_FAILURE, listener.event);
    }

    /**
     * 响应解析失败静默失败
     */
    @Test
    public void testGarbageBody() throws Exception {
        body.set("not a json");
        var listener = new LatchCheckListener();
        UpdateChecker.startCheck(listener);
        assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        assertEquals(EVENT_FAILURE, listener.event);
    }

    /**
     * 网络异常(服务不可达)静默失败
     */
    @Test
    public void testConnectionRefused() throws Exception {
        var port = server.getAddress().getPort();
        server.stop(0);
        UpdateChecker.overrideApiUrl("http://127.0.0.1:" + port + "/releases/latest");
        var listener = new LatchCheckListener();
        UpdateChecker.startCheck(listener);
        assertTrue(listener.latch.await(10, TimeUnit.SECONDS));
        assertEquals(EVENT_FAILURE, listener.event);
    }

    /**
     * 检查进行中重复触发被忽略
     */
    @Test
    public void testBusyGuard() throws Exception {
        slow.set(true);
        var first = new LatchCheckListener();
        UpdateChecker.startCheck(first);
        // 等待首个检查真正进入工作线程
        Thread.sleep(200);
        var second = new LatchCheckListener();
        UpdateChecker.startCheck(second);
        assertFalse("检查进行中第二次触发应被忽略", second.latch.await(1, TimeUnit.SECONDS));
        assertTrue(first.latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * 构造最小releases样例JSON
     * @param tag tag名
     * @param assetName 资产名
     * @param assetUrl 资产下载地址
     * @return JSON串
     */
    private static String releaseJson(String tag, String assetName, String assetUrl) {
        return String.format(
                "{ \"tag_name\": \"%s\", \"name\": \"x\", \"prerelease\": false,"
                        + " \"assets\": [ { \"name\": \"%s\", \"browser_download_url\": \"%s\", \"size\": 1024 } ],"
                        + " \"body\": \"notes\" }",
                tag, assetName, assetUrl);
    }

    /**
     * 锁存回调收集器
     */
    private static final class LatchCheckListener implements UpdateChecker.CheckListener {
        private final CountDownLatch latch   = new CountDownLatch(1);
        private volatile int         event   = -1;
        private volatile String      version;
        private volatile String      url;

        @Override
        public void onUpdateAvailable(String version, String dmgUrl) {
            this.version = version;
            this.url = dmgUrl;
            event = EVENT_AVAILABLE;
            latch.countDown();
        }

        @Override
        public void onNoUpdate() {
            event = EVENT_NO_UPDATE;
            latch.countDown();
        }

        @Override
        public void onFailure() {
            event = EVENT_FAILURE;
            latch.countDown();
        }
    }
}
