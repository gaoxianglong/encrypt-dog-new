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

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * logo资源加载工具,盾牌图标(SVG经qlmanage转换为PNG)随jar发布
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/26 14:00
 */
public final class LogoUtil {
    /**
     * 图标缓存(资源+尺寸+内框因子为键):避免在paint路径中反复解码/扫描/缩放图片
     */
    private static final Map<String, ImageIcon> ICON_CACHE = new ConcurrentHashMap<>();

    /**
     * logo内框因子:内容收进显示槽位的约82%内框,留出与macOS标准图标惯例
     * 一致的约18%视觉留白(与Dock路径loadRoundedImage同约定),避免观感偏大。
     * 托盘与Dock均沿用此约定(托盘22pt槽位内容约18pt)
     */
    private static final double LOGO_INSET = 0.82;

    /**
     * 标题栏logo内框因子:标题栏槽位(28px)远大于品牌文字(22pt)视觉高度,
     * 0.82内框下logo仍压制文字,收进约64%内框后图形约18px与文字平衡
     */
    private static final double LOGO_TITLE_INSET = 0.64;

    private LogoUtil() {
    }

    /**
     * 加载指定显示尺寸的logo图标,图片等比缩放适配显示区域。
     * logo内容收进约82%内框(留约18%视觉留白),Icon宽高不超过内框且保持
     * 原图宽高比,JLabel对超尺寸Icon是裁切而非缩放,等比缩放后Icon不超槽位,
     * 居中显示不变形。
     * @param size 显示尺寸(逻辑像素)
     * @return
     */
    public static ImageIcon loadLogo(int size) {
        return loadImage(UiConstants.LOGO_RESOURCE, size, LOGO_INSET);
    }

    /**
     * 加载标题栏logo图标:内容收进约64%内框,图形约18px与22pt品牌文字视觉平衡。
     * 其余行为与loadLogo一致(等比缩放、不超槽位、居中显示)
     * @param size 显示尺寸(逻辑像素)
     * @return
     */
    public static ImageIcon loadTitleLogo(int size) {
        return loadImage(UiConstants.LOGO_RESOURCE, size, LOGO_TITLE_INSET);
    }

    /**
     * 按显示区域等比缩放加载资源图片,保持原图宽高比且不超显示尺寸,
     * 避免非方形图片被拉伸变形或超尺寸被裁切
     * @param resource 资源路径(如logo.png)
     * @param size 显示尺寸(逻辑像素)
     * @return
     */
    public static ImageIcon loadImage(String resource, int size) {
        return loadImage(resource, size, 1.0);
    }

    /**
     * 按显示区域等比缩放加载资源图片,缩放基准为显示尺寸乘内框因子,
     * 保持原图宽高比且不超缩放基准,避免非方形图片被拉伸变形或超尺寸被裁切
     * @param resource 资源路径(如logo.png)
     * @param size 显示尺寸(逻辑像素)
     * @param insetFactor 内框因子(1.0=顶满槽位,0.82=收进82%内框)
     * @return
     */
    private static ImageIcon loadImage(String resource, int size, double insetFactor) {
        String key = resource + "@" + size + "@" + insetFactor;
        ImageIcon cached = ICON_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        try (InputStream in = LogoUtil.class.getClassLoader().getResourceAsStream(resource)) {
            if (Objects.isNull(in)) {
                return null;
            }
            var image = ImageIO.read(in);
            // 裁掉透明边: 按alpha包围盒取内容区域,避免大画布透明边距挤压图形
            var bounds = alphaBounds(image);
            var content = Objects.isNull(bounds)
                    ? image : image.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
            // 等比缩放: 以宽高中较小者为基准,缩放基准=显示尺寸×内框因子,Icon不超槽位,由JLabel居中显示
            var base = size * insetFactor;
            var scale = Math.min(base / content.getWidth(), base / content.getHeight());
            var width = (int) Math.round(content.getWidth() * scale);
            var height = (int) Math.round(content.getHeight() * scale);
            // 同步绘制缩放,规避getScaledInstance异步缩放未完成导致的残缺渲染
            var scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(content, 0, 0, width, height, null);
            g2d.dispose();
            var icon = new ImageIcon(scaled);
            ICON_CACHE.put(key, icon);
            return icon;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 圆角渲染加载资源图片:等比缩放至目标尺寸并以圆角矩形裁剪(四角透明),
     * 内容收进82%内框(与macOS标准图标自带约18%透明边距一致,避免Dock观感偏大),
     * 半径=内框边长22%(与macOS标准squircle观感一致,与打包脚本PIL预处理同比例)。
     * headless安全(BufferedImage/Graphics2D不初始化AWT Toolkit),可在AWT初始化前调用
     * @param resource 资源路径(如dock_logo.png)
     * @param size 输出尺寸(像素)
     * @return 圆角渲染后的图像,资源缺失返回null
     */
    public static BufferedImage loadRoundedImage(String resource, int size) {
        try (InputStream in = LogoUtil.class.getClassLoader().getResourceAsStream(resource)) {
            if (Objects.isNull(in)) {
                return null;
            }
            var image = ImageIO.read(in);
            var bounds = alphaBounds(image);
            var content = Objects.isNull(bounds)
                    ? image : image.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
            var inner = (int) Math.round(size * 0.82);
            var inset = (size - inner) / 2;
            var scale = Math.min((double) inner / content.getWidth(), (double) inner / content.getHeight());
            var width = (int) Math.round(content.getWidth() * scale);
            var height = (int) Math.round(content.getHeight() * scale);
            var out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            var g2d = out.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            // 圆角裁剪(半径=内框边长22%),居中绘制缩放后的内容
            var arc = (int) Math.round(inner * 0.22);
            g2d.clip(new java.awt.geom.RoundRectangle2D.Double(inset, inset, inner, inner, arc, arc));
            g2d.drawImage(content, inset + (inner - width) / 2, inset + (inner - height) / 2, width, height, null);
            g2d.dispose();
            return out;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 计算图片非透明内容的包围盒(alpha>0),全透明时返回null
     * @param image
     * @return
     */
    private static Rectangle alphaBounds(BufferedImage image) {
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
        if (maxX < 0) {
            return null;
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
