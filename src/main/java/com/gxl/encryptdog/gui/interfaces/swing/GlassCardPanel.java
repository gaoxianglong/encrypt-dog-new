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

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
/**
 * 毛玻璃卡片：半透明圆角 + 阴影 + 高光描边，支持内容整体淡入淡出。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class GlassCardPanel extends JPanel {
    /**
     * 阴影横向偏移
     */
    private static final int    SHADOW_OFFSET_X      = 5;
    /**
     * 阴影纵向偏移
     */
    private static final int    SHADOW_OFFSET_Y      = 8;
    /**
     * 阴影收缩量（两侧各留出阴影空间）
     */
    private static final int    SHADOW_INSET         = 6;
    /**
     * 描边宽度
     */
    private static final float  BORDER_WIDTH        = 1.2F;
    /**
     * 内容不透明度（0~1）
     */
    private float               contentAlpha        = 1.0F;

    public GlassCardPanel() {
        setOpaque(false);
        setLayout(null);
    }

    /**
     * 设置内容整体不透明度（用于淡入淡出动画）。
     *
     * @param contentAlpha 不透明度（0~1）
     */
    public void setContentAlpha(float contentAlpha) {
        this.contentAlpha = contentAlpha;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, contentAlpha));
        super.paint(g2d);
        g2d.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 阴影
        g2d.setColor(UiConstants.CARD_SHADOW);
        g2d.fillRoundRect(SHADOW_OFFSET_X, SHADOW_OFFSET_Y,
                width - SHADOW_INSET * 2, height - SHADOW_INSET * 2,
                UiConstants.CARD_ARC, UiConstants.CARD_ARC);

        // 半透明主体
        g2d.setColor(UiConstants.CARD_FILL);
        g2d.fillRoundRect(0, 0, width, height, UiConstants.CARD_ARC, UiConstants.CARD_ARC);

        // 描边
        g2d.setColor(UiConstants.CARD_BORDER);
        g2d.setStroke(new BasicStroke(BORDER_WIDTH));
        g2d.drawRoundRect(0, 0, width - 1, height - 1, UiConstants.CARD_ARC, UiConstants.CARD_ARC);

        // 顶部高光描边带已取消(其底边在蒙层上形成分割线),蒙层为干净统一的半透明面
        g2d.dispose();
    }
}
