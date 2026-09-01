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
 *  *
 */

package com.gxl.encryptdog.gui.interfaces.swing;

import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;

import javax.swing.JPanel;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * 静态背景层：纯紫色渐变（BG_TOP 近黑深蓝 → BG_BOTTOM 深紫，顶部与标题栏无缝衔接）。
 * 无 Timer、无动画、无交互，仅在尺寸变化时重建预渲染缓存，空闲零开销。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/1 22:00
 */
public class GradientBackgroundPanel extends JPanel {
    /**
     * 背景渐变缓存（尺寸变化时重建）
     */
    private BufferedImage backgroundCache;
    /**
     * 缓存宽度（尺寸变化判定）
     */
    private int            cacheWidth       = -1;
    /**
     * 缓存高度（尺寸变化判定）
     */
    private int            cacheHeight      = -1;

    public GradientBackgroundPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        if (backgroundCache == null || cacheWidth != width || cacheHeight != height) {
            backgroundCache = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = backgroundCache.createGraphics();
            bg.setPaint(new GradientPaint(0, 0, UiConstants.BG_TOP, 0, height, UiConstants.BG_BOTTOM));
            bg.fillRect(0, 0, width, height);
            bg.dispose();
            cacheWidth = width;
            cacheHeight = height;
        }
        g.drawImage(backgroundCache, 0, 0, null);
    }
}
