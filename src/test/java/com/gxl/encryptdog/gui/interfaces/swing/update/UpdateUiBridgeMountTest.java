package com.gxl.encryptdog.gui.interfaces.swing.update;

import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * DMG挂载备选路径单元测试(任务2.2):hdiutil attach输出挂载点解析,
 * 以及真实hdiutil挂载往返(hdiutil可用时)
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/4 15:00
 */
public class UpdateUiBridgeMountTest {
    /**
     * 标准hdiutil attach输出解析出/Volumes挂载点
     */
    @Test
    public void testParseMountPoint() {
        var output = "expected   CRC32 $A1B2C3D4\n"
                + "/dev/disk4    \tGUID_partition_scheme          \n"
                + "/dev/disk4s1  \tApple_HFS                       \t/Volumes/EncryptDog 2.1.0\n";
        assertEquals("/Volumes/EncryptDog 2.1.0", UpdateUiBridge.parseMountPoint(output));
    }

    /**
     * 无法解析的输出返回null
     */
    @Test
    public void testParseMountPointGarbage() {
        assertNull(UpdateUiBridge.parseMountPoint("garbage output"));
        assertNull(UpdateUiBridge.parseMountPoint(null));
        assertNull(UpdateUiBridge.parseMountPoint(""));
    }

    /**
     * 真实hdiutil挂载往返:创建临时dmg、attach、解析挂载点、detach
     * (仅hdiutil可用时执行,验证备选链路的进程调用与解析逻辑)
     */
    @Test
    public void testHdiutilRoundTrip() throws Exception {
        Assume.assumeTrue("hdiutil not available", hdiutilAvailable());
        var dmg = Files.createTempFile("encryptdog-update-test", ".dmg").toFile();
        dmg.delete();
        try {
            // 创建1MB测试dmg
            var create = new ProcessBuilder("hdiutil", "create", "-size", "1m", "-fs", "HFS+", "-volname",
                    "EncryptDogTest", dmg.getAbsolutePath()).start();
            assertTrue(create.waitFor(60, TimeUnit.SECONDS));
            assertEquals(0, create.exitValue());

            // attach并解析挂载点(与UpdateUiBridge.openDmg备选路径同形)
            var attach = new ProcessBuilder("hdiutil", "attach", dmg.getAbsolutePath()).start();
            assertTrue(attach.waitFor(60, TimeUnit.SECONDS));
            assertEquals(0, attach.exitValue());
            var mountPoint = UpdateUiBridge.parseMountPoint(
                    new String(attach.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            assertTrue(mountPoint != null && mountPoint.startsWith("/Volumes/"));
            assertTrue(new File(mountPoint).isDirectory());

            // detach清理
            var detach = new ProcessBuilder("hdiutil", "detach", mountPoint).start();
            assertTrue(detach.waitFor(60, TimeUnit.SECONDS));
        } finally {
            dmg.delete();
        }
    }

    /**
     * hdiutil是否可用
     * @return true=可用
     */
    private static boolean hdiutilAvailable() {
        try {
            var process = new ProcessBuilder("hdiutil", "version").start();
            return process.waitFor(10, TimeUnit.SECONDS) && 0 == process.exitValue();
        } catch (Throwable e) {
            return false;
        }
    }
}
