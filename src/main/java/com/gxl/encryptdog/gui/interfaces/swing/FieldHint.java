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

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 字段内联错误提示:主题紫文字 + 半透明深色背衬,
 * 展示约1.2秒后逐步淡出(0.8秒),总时长约2秒,重复showHint重新计时。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/27 12:40
 */
public class FieldHint extends JComponent {
    /**
     * 完全展示时长(毫秒)
     */
    private static final long   VISIBLE_MS       = 1200;
    /**
     * 淡出时长(毫秒)
     */
    private static final long   FADE_MS          = 800;
    /**
     * 总时长(毫秒)
     */
    private static final long   TOTAL_MS         = VISIBLE_MS + FADE_MS;
    /**
     * 文字水平内边距
     */
    public static final int     HINT_PADDING_X   = 12;
    /**
     * 背衬圆角半径
     */
    private static final int    ARC              = 10;
    /**
     * 半透明深色背衬(与字段内容重叠时可读),轻量不显重,随主色系派生的共享深紫
     */
    private static final Color  BACKING          = new Color(UiConstants.DEEP_ACCENT.getRed(), UiConstants.DEEP_ACCENT.getGreen(),
            UiConstants.DEEP_ACCENT.getBlue(), 170);
    /**
     * 提示文字颜色:亮薰衣草紫,主题色系且浅于ACCENT_BRIGHT
     */
    private static final Color  HINT_FOREGROUND  = new Color(0xCB, 0xBD, 0xF1);
    /**
     * 默认字体键
     */
    private static final String DEFAULT_FONT_KEY = "defaultFont";
    /**
     * 本次展示的起始时间
     */
    private long                startMs;
    /**
     * 动画计时器
     */
    private final Timer         timer;
    /**
     * 提示文案
     */
    private String              hintText          = "";

    public FieldHint() {
        setOpaque(false);
        setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
        timer = new Timer(UiConstants.FRAME_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (System.currentTimeMillis() - startMs >= TOTAL_MS) {
                    setVisible(false);
                    timer.stop();
                }
                repaint();
            }
        });
        setVisible(false);
    }

    /**
     * 展示提示文案并重新计时
     * @param text 提示文案
     */
    public void showHint(String text) {
        hintText = text;
        startMs = System.currentTimeMillis();
        setVisible(true);
        timer.restart();
        repaint();
    }

    /**
     * 隐藏提示
     */
    public void dismiss() {
        timer.stop();
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 展示1.2秒后逐步淡出
        long elapsed = System.currentTimeMillis() - startMs;
        float alpha = 1F;
        if (elapsed >= VISIBLE_MS) {
            alpha = Math.max(0F, 1F - (float) (elapsed - VISIBLE_MS) / FADE_MS);
        }
        if (alpha <= 0F) {
            g2d.dispose();
            return;
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // 半透明深色背衬
        g2d.setColor(BACKING);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

        // 亮薰衣草紫文字,左对齐留内边距,垂直居中
        g2d.setColor(HINT_FOREGROUND);
        g2d.setFont(getFont());
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.drawString(hintText, HINT_PADDING_X, (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
        g2d.dispose();
    }
}
