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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 密码输入框：内嵌眼睛图标按钮，点击在掩码与明文显示间切换，切换保留已输入内容与焦点。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/26 14:00
 */
public class PasswordToggleField extends JPanel {
    /**
     * 切换按钮宽度
     */
    private static final int     TOGGLE_BUTTON_WIDTH = 40;
    /**
     * 掩码密码框
     */
    private final JPasswordField passwordField       = new JPasswordField();
    /**
     * 明文输入框
     */
    private final JTextField     plainField          = new JTextField();
    /**
     * 眼睛切换按钮
     */
    private final JButton        toggleButton        = new JButton();
    /**
     * 当前是否明文显示
     */
    private boolean              plainVisible;

    public PasswordToggleField() {
        setOpaque(false);
        setLayout(new BorderLayout());
        add(passwordField, BorderLayout.CENTER);

        toggleButton.setPreferredSize(new Dimension(TOGGLE_BUTTON_WIDTH, TOGGLE_BUTTON_WIDTH));
        toggleButton.setOpaque(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setFocusPainted(false);
        // 眼睛按钮不参与Tab焦点遍历:仅鼠标触发,避免Tab第一下落在按钮上导致密码框需两次Tab才离开
        toggleButton.setFocusable(false);
        toggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggle();
            }
        });
        add(toggleButton, BorderLayout.EAST);
        // 初始为掩码态,图标为睁眼(点击显示明文)
        refreshToggleState();
    }

    /**
     * 切换掩码/明文显示,保留已输入内容与焦点
     */
    public void toggle() {
        plainVisible = !plainVisible;
        if (plainVisible) {
            // 拷贝掩码框内容到明文框
            plainField.setText(new String(passwordField.getPassword()));
            remove(passwordField);
            add(plainField, BorderLayout.CENTER);
            plainField.requestFocusInWindow();
        } else {
            // 拷贝明文框内容回掩码框
            passwordField.setText(plainField.getText());
            remove(plainField);
            add(passwordField, BorderLayout.CENTER);
            passwordField.requestFocusInWindow();
        }
        refreshToggleState();
        revalidate();
        repaint();
    }

    /**
     * 清空密码内容,兼容掩码态与明文态
     */
    public void clear() {
        passwordField.setText("");
        plainField.setText("");
    }

    /**
     * 获取密码内容,明文态与掩码态语义一致
     * @return
     */
    public char[] getPassword() {
        if (plainVisible) {
            return plainField.getText().toCharArray();
        }
        return passwordField.getPassword();
    }

    /**
     * 刷新切换按钮的图标与提示
     */
    private void refreshToggleState() {
        // 掩码态显示睁眼图标(点击显示明文),明文态显示闭眼图标(点击隐藏)
        toggleButton.setIcon(new EyeIcon(!plainVisible));
        toggleButton.setToolTipText(plainVisible ? "Hide password" : "Show password");
    }

    /**
     * 眼睛图标:睁眼(椭圆+瞳孔)/闭眼(椭圆+斜线),Java2D自绘
     */
    private static final class EyeIcon implements Icon {
        /**
         * 图标边长
         */
        private static final int  ICON_SIZE = 22;
        /**
         * 是否睁眼
         */
        private final boolean     open;

        private EyeIcon(boolean open) {
            this.open = open;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(UiConstants.TEXT_SECONDARY);
            g2d.setStroke(new BasicStroke(1.6F));
            if (open) {
                // 眼轮廓
                g2d.drawOval(x + 2, y + 6, ICON_SIZE - 4, ICON_SIZE - 12);
                // 瞳孔
                g2d.fillOval(x + ICON_SIZE / 2 - 2, y + ICON_SIZE / 2 - 2, 4, 4);
            } else {
                // 眼轮廓
                g2d.drawOval(x + 2, y + 6, ICON_SIZE - 4, ICON_SIZE - 12);
                // 斜线表示隐藏
                g2d.drawLine(x + 3, y + ICON_SIZE - 3, x + ICON_SIZE - 3, y + 3);
            }
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return ICON_SIZE;
        }

        @Override
        public int getIconHeight() {
            return ICON_SIZE;
        }
    }
}
