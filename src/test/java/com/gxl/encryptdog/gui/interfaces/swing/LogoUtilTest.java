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

package com.gxl.encryptdog.gui.interfaces.swing;

import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * LogoUtil内框渲染单元测试(任务1.2)
 * <p>
 * 源图logo.png内容包围盒(alpha&gt;0)宽高比已知为宽限轴,断言loadLogo输出
 * 宽度=round(槽位×0.82)、loadImage保持顶满槽位语义、等比缩放不变形。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/5 15:00
 */
public class LogoUtilTest {
    /**
     * logo内框因子(与LogoUtil.LOGO_INSET同约定)
     */
    private static final double LOGO_INSET = 0.82;

    /**
     * 22/28/44三档:内容收进约82%内框,宽度=round(槽位×0.82)且不超内框
     */
    @Test
    public void testLoadLogoInsetWidth() {
        for (var size : new int[]{22, 28, 44}) {
            var icon = LogoUtil.loadLogo(size);
            assertNotNull("槽位" + size + "的logo不应为null", icon);
            var expected = (int) Math.round(size * LOGO_INSET);
            assertEquals("槽位" + size + "的logo宽度应为82%内框", expected, icon.getIconWidth());
            assertTrue("槽位" + size + "的logo高度不应超过内框", icon.getIconHeight() <= expected);
        }
    }

    /**
     * 等比缩放:logo输出宽高比与源图alpha包围盒宽高比一致(不拉伸变形)
     */
    @Test
    public void testLoadLogoAspectRatioPreserved() throws Exception {
        var sourceAspect = sourceContentAspect();
        for (var size : new int[]{22, 28, 44}) {
            var icon = LogoUtil.loadLogo(size);
            var width = icon.getIconWidth();
            var height = icon.getIconHeight();
            // 宽高比在取整误差内保持一致:height×源宽 ≈ width×源高(容差=源宽)
            assertTrue("槽位" + size + "的logo宽高比不应变形",
                    Math.abs(height * sourceAspect[0] - width * sourceAspect[1]) <= sourceAspect[0]);
        }
    }

    /**
     * 标题栏档位:槽位28px内容收进约64%内框(宽度=round(28×0.64)=18),
     * 与托盘档位(82%)相互独立不串扰
     */
    @Test
    public void testLoadTitleLogoInset() {
        var icon = LogoUtil.loadTitleLogo(28);
        assertNotNull(icon);
        assertEquals("标题栏logo宽度应为64%内框", 18, icon.getIconWidth());
        assertTrue("标题栏logo高度不应超过内框", icon.getIconHeight() <= 18);
        // 托盘档位不受影响
        assertEquals("托盘档位应保持82%内框", 18, LogoUtil.loadLogo(22).getIconWidth());
    }

    /**
     * 通用loadImage回归:同尺寸仍为顶满槽位语义(内框因子1.0),未被logo约定污染
     */
    @Test
    public void testLoadImageFullBleedRegression() {
        var icon = LogoUtil.loadImage(UiConstants.LOGO_RESOURCE, 22);
        assertNotNull(icon);
        assertEquals("通用路径应顶满槽位宽度", 22, icon.getIconWidth());
    }

    /**
     * 资源缺失返回null(Error场景)
     */
    @Test
    public void testLoadImageMissingResourceNull() {
        assertNull(LogoUtil.loadImage("nonexistent-resource.png", 22));
    }

    /**
     * 读取源图logo.png的alpha包围盒宽高(宽, 高)
     */
    private int[] sourceContentAspect() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(UiConstants.LOGO_RESOURCE)) {
            assertNotNull("测试资源logo.png缺失", in);
            BufferedImage image = ImageIO.read(in);
            var width = image.getWidth();
            var height = image.getHeight();
            var pixels = image.getRGB(0, 0, width, height, null, 0, width);
            int minX = width, minY = height, maxX = -1, maxY = -1;
            for (var y = 0; y < height; y++) {
                var row = y * width;
                for (var x = 0; x < width; x++) {
                    if ((pixels[row + x] >>> 24) > 0) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            assertTrue("源图存在不透明内容", maxX >= 0);
            return new int[]{maxX - minX + 1, maxY - minY + 1};
        }
    }
}
