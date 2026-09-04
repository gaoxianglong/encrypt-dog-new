package com.gxl.encryptdog.gui.infrastructure.update;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 版本归一化与比较单元测试(任务1.1)
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/4 15:00
 */
public class VersionComparatorTest {
    /**
     * 高版本(v前缀)高于当前版本
     */
    @Test
    public void testNewerWithVPrefix() {
        assertTrue(VersionComparator.isNewer("2.0.5", "v2.1.0"));
    }

    /**
     * 相同版本不判定为新版本
     */
    @Test
    public void testSameVersion() {
        assertFalse(VersionComparator.isNewer("2.0.5", "v2.0.5"));
        assertFalse(VersionComparator.isNewer("2.0.5", "2.0.5"));
    }

    /**
     * -RELEASE后缀剥离后正常比较
     */
    @Test
    public void testReleaseSuffix() {
        assertTrue(VersionComparator.isNewer("2.0.5", "2.1.0-RELEASE"));
        assertFalse(VersionComparator.isNewer("2.0.5", "2.0.5-RELEASE"));
    }

    /**
     * 任一侧不可解析均视为无新版本
     */
    @Test
    public void testUnparseable() {
        assertFalse(VersionComparator.isNewer("2.0.5", "abc"));
        assertFalse(VersionComparator.isNewer("abc", "2.0.5"));
        assertFalse(VersionComparator.isNewer("2.0.5", null));
        assertFalse(VersionComparator.isNewer(null, "2.1.0"));
    }

    /**
     * 两位版本号数值比较而非字典序
     */
    @Test
    public void testNumericCompare() {
        assertTrue(VersionComparator.isNewer("2.9.0", "2.10.0"));
        assertFalse(VersionComparator.isNewer("2.10.0", "2.9.0"));
    }
}
