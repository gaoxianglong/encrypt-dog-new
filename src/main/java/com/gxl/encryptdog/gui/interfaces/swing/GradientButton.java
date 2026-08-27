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

import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 渐变主按钮：ACCENT 渐变背景、悬停提亮、禁用置灰，支持加载态旋转弧。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class GradientButton extends JButton {
    /**
     * 按钮圆角半径
     */
    private static final int    BUTTON_ARC          = 22;
    /**
     * 悬停提亮系数
     */
    private static final float  HOVER_LIGHTEN_FACTOR = 0.18F;
    /**
     * 加载旋转弧半径
     */
    private static final int    SPINNER_RADIUS      = 8;
    /**
     * 加载旋转弧角度跨度（度）
     */
    private static final int    SPINNER_ARC_DEGREES = 260;
    /**
     * 加载旋转周期（毫秒）
     */
    private static final int    SPINNER_PERIOD_MS   = 900;
    /**
     * 旋转弧与文字间距
     */
    private static final int    SPINNER_TEXT_GAP    = 10;
    /**
     * 旋转弧线宽
     */
    private static final float  SPINNER_STROKE_WIDTH = 2.4F;
    /**
     * 颜色通道最大值
     */
    private static final int    MAX_RGB             = 255;
    /**
     * 完整圆周角度（度）
     */
    private static final double FULL_CIRCLE_DEGREES = 360.0;
    /**
     * 默认字体键
     */
    private static final String DEFAULT_FONT_KEY    = "defaultFont";
    /**
     * 默认文本
     */
    private final String        defaultText;
    /**
     * 是否悬停
     */
    private boolean             hovered;
    /**
     * 是否加载中
     */
    private boolean             loading;
    /**
     * 加载动画计时器
     */
    private final Timer         loadingTimer;

    /**
     * 构造渐变按钮。
     *
     * @param text 按钮文本
     */
    public GradientButton(String text) {
        super(text);
        this.defaultText = text;
        // 加载态旋转弧重绘
        loadingTimer = new Timer(UiConstants.FRAME_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.BOLD, UiConstants.BODY_FONT_SIZE));
        setForeground(UiConstants.BUTTON_TEXT);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    /**
     * 设置加载态：展示旋转弧并禁用按钮。
     *
     * @param loading     是否加载中
     * @param loadingText 加载中文案
     */
    public void setLoading(boolean loading, String loadingText) {
        this.loading = loading;
        setEnabled(!loading);
        setText(loading ? loadingText : defaultText);
        if (loading) {
            loadingTimer.start();
        } else {
            loadingTimer.stop();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintGradient(g2d);
        if (loading) {
            paintSpinner(g2d);
        }
        g2d.dispose();
        super.paintComponent(g);
    }

    /**
     * 绘制渐变圆角背景。
     *
     * @param g2d 绘图上下文
     */
    private void paintGradient(Graphics2D g2d) {
        Color startColor = UiConstants.ACCENT;
        Color endColor = UiConstants.ACCENT_BRIGHT;
        if (!isEnabled()) {
            startColor = UiConstants.BUTTON_DISABLED;
            endColor = UiConstants.BUTTON_DISABLED;
        } else if (hovered) {
            startColor = UiConstants.ACCENT_BRIGHT;
            endColor = lighten(UiConstants.ACCENT_BRIGHT, HOVER_LIGHTEN_FACTOR);
        }
        g2d.setPaint(new GradientPaint(0, 0, startColor, getWidth(), 0, endColor));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC);
    }

    /**
     * 绘制加载旋转弧（位于文字左侧）。
     *
     * @param g2d 绘图上下文
     */
    private void paintSpinner(Graphics2D g2d) {
        FontMetrics metrics = g2d.getFontMetrics(getFont());
        int textWidth = metrics.stringWidth(getText());
        int spinnerX = (getWidth() - textWidth) / 2 - SPINNER_RADIUS * 2 - SPINNER_TEXT_GAP;
        int spinnerY = (getHeight() - SPINNER_RADIUS * 2) / 2;
        double angle = (double) (System.currentTimeMillis() % SPINNER_PERIOD_MS)
                / SPINNER_PERIOD_MS * FULL_CIRCLE_DEGREES;
        g2d.setColor(UiConstants.BUTTON_TEXT);
        g2d.setStroke(new BasicStroke(SPINNER_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawArc(spinnerX, spinnerY, SPINNER_RADIUS * 2, SPINNER_RADIUS * 2,
                (int) Math.round(angle), SPINNER_ARC_DEGREES);
    }

    /**
     * 按系数向白色方向提亮颜色。
     *
     * @param color  原颜色
     * @param factor 提亮系数（0~1）
     * @return 提亮后的颜色
     */
    private static Color lighten(Color color, float factor) {
        int red = Math.min(MAX_RGB, (int) (color.getRed() + (MAX_RGB - color.getRed()) * factor));
        int green = Math.min(MAX_RGB, (int) (color.getGreen() + (MAX_RGB - color.getGreen()) * factor));
        int blue = Math.min(MAX_RGB, (int) (color.getBlue() + (MAX_RGB - color.getBlue()) * factor));
        return new Color(red, green, blue);
    }
}
