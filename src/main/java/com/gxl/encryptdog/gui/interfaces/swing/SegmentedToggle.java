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
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 等宽分段选择器:各分段等宽,选中段ACCENT填充白字,未选段透明。
 * 用于加/解密模式切换。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/26 15:50
 */
public class SegmentedToggle extends JPanel {
    /**
     * 默认字体键
     */
    private static final String DEFAULT_FONT_KEY = "defaultFont";
    /**
     * 圆角半径
     */
    private static final int    ARC_RADIUS       = 10;
    /**
     * 分段文本
     */
    private final String[]      items;
    /**
     * 当前选中分段索引
     */
    private int                 selectedIndex;
    /**
     * 选择变化监听
     */
    private ChangeListener      changeListener;

    /**
     * 构造分段选择器
     * @param items 分段文本
     */
    public SegmentedToggle(String[] items) {
        this.items = items;
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 按点击位置计算分段索引
                int index = e.getX() * items.length / getWidth();
                if (index < 0) {
                    index = 0;
                }
                if (index >= items.length) {
                    index = items.length - 1;
                }
                setSelectedIndex(index);
            }
        });
    }

    /**
     * 设置选中分段,变化时触发监听
     * @param index
     */
    public void setSelectedIndex(int index) {
        if (index == selectedIndex) {
            return;
        }
        selectedIndex = index;
        repaint();
        if (changeListener != null) {
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * 获取选中分段索引
     * @return
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * 注册选择变化监听
     * @param listener
     */
    public void addChangeListener(ChangeListener listener) {
        this.changeListener = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int segmentWidth = width / items.length;

        // 整体暗色圆角底
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(0, 0, width, height, ARC_RADIUS, ARC_RADIUS);

        // 选中段ACCENT圆角填充
        int selectedX = selectedIndex * segmentWidth;
        g2d.setColor(UiConstants.ACCENT);
        g2d.fillRoundRect(selectedX + 2, 2, segmentWidth - 4, height - 4, ARC_RADIUS - 2, ARC_RADIUS - 2);

        // 分段文本
        g2d.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.BOLD, UiConstants.BODY_FONT_SIZE));
        FontMetrics metrics = g2d.getFontMetrics();
        for (int i = 0; i < items.length; i++) {
            String text = items[i];
            int textX = i * segmentWidth + (segmentWidth - metrics.stringWidth(text)) / 2;
            int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
            g2d.setColor(i == selectedIndex ? UiConstants.BUTTON_TEXT : UiConstants.TEXT_SECONDARY);
            g2d.drawString(text, textX, textY);
        }
        g2d.dispose();
    }
}
