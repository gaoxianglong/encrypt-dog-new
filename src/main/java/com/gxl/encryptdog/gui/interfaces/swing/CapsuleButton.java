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
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 胶囊按钮：扁平主色实底、白色文字、全圆角（弧=高/2）、悬停提亮、禁用置灰。
 * 表单添加类小按钮专用，层级介于渐变主按钮（GradientButton）与幽灵文字按钮之间。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/1 12:00
 */
public class CapsuleButton extends JButton {
    /**
     * 悬停提亮系数
     */
    private static final float  HOVER_LIGHTEN_FACTOR = 0.18F;
    /**
     * 颜色通道最大值
     */
    private static final int    MAX_RGB             = 255;
    /**
     * 默认字体键
     */
    private static final String DEFAULT_FONT_KEY    = "defaultFont";
    /**
     * 是否悬停
     */
    private boolean             hovered;

    /**
     * 构造胶囊按钮。
     *
     * @param text 按钮文本
     */
    public CapsuleButton(String text) {
        super(text);
        setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 扁平主色实底:常态主色,悬停提亮,禁用置灰;弧=高/2全胶囊
        Color fill = UiConstants.ACCENT;
        if (!isEnabled()) {
            fill = UiConstants.BUTTON_DISABLED;
        } else if (hovered) {
            fill = lighten(UiConstants.ACCENT, HOVER_LIGHTEN_FACTOR);
        }
        g2d.setColor(fill);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2d.dispose();
        super.paintComponent(g);
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
