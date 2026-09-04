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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 主题化确认弹窗:无边框圆角模态对话框,紫色渐变背景与主题按钮,
 * 与主窗口视觉风格一致。关闭/ESC等价于选择次要按钮(返回false)。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/27 11:00
 */
public class ThemedConfirmDialog extends JDialog {
    /**
     * 弹窗宽度
     */
    private static final int     WIDTH               = 440;
    /**
     * 弹窗高度(双按钮)
     */
    private static final int     HEIGHT              = 210;
    /**
     * 弹窗高度(单按钮紧凑模式)
     */
    private static final int     HEIGHT_COMPACT      = 160;
    /**
     * 弹窗圆角半径
     */
    private static final int     ARC                 = 24;
    /**
     * 描边宽度
     */
    private static final float   BORDER_WIDTH        = 1.2F;
    /**
     * 内容左右内边距
     */
    private static final int     PADDING_X           = 28;
    /**
     * 按钮宽度
     */
    private static final int     BUTTON_WIDTH        = 130;
    /**
     * 按钮高度
     */
    private static final int     BUTTON_HEIGHT       = 38;
    /**
     * 按钮行下边距
     */
    private static final int     BUTTON_BOTTOM_GAP   = 24;
    /**
     * 按钮间距
     */
    private static final int     BUTTON_GAP          = 12;
    /**
     * 描边按钮圆角半径
     */
    private static final int     OUTLINE_ARC         = 19;
    /**
     * 描边按钮线宽
     */
    private static final float   OUTLINE_STROKE      = 1.4F;
    /**
     * 默认字体键
     */
    private static final String  DEFAULT_FONT_KEY    = "defaultFont";
    /**
     * ESC动作键
     */
    private static final String  CLOSE_ACTION_KEY    = "close";
    /**
     * 是否点击确认按钮
     */
    private boolean              confirmed;

    private ThemedConfirmDialog(Window owner, String title, String message, String subMessage,
            String confirmText, String keepText) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        // 窗口背景全透明,圆角外区域不出现系统默认灰色
        setBackground(new Color(0, 0, 0, 0));
        // keepText为空时单按钮模式:紧凑高度、无副文案
        var singleButton = null == keepText || keepText.isEmpty();
        setSize(WIDTH, singleButton ? HEIGHT_COMPACT : HEIGHT);
        setLocationRelativeTo(owner);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 紫色渐变背景,与整体主题一致
                g2d.setPaint(new GradientPaint(0, 0, UiConstants.BG_TOP, 0, getHeight(), UiConstants.BG_BOTTOM));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                // 描边
                g2d.setColor(UiConstants.CARD_BORDER);
                g2d.setStroke(new BasicStroke(BORDER_WIDTH));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
                g2d.dispose();
            }
        };
        content.setLayout(null);
        // 内容面板不透明会在四角涂出面板默认底色
        content.setOpaque(false);

        // 标题
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UiConstants.TEXT_PRIMARY);
        titleLabel.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.BOLD, 18F));
        titleLabel.setBounds(PADDING_X, 24, WIDTH - PADDING_X * 2, 26);
        content.add(titleLabel);

        // 主文案
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(UiConstants.TEXT_PRIMARY);
        messageLabel.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.BODY_FONT_SIZE));
        messageLabel.setBounds(PADDING_X, 64, WIDTH - PADDING_X * 2, 22);
        content.add(messageLabel);

        // 副文案(单按钮模式无副文案)
        if (!singleButton) {
            JLabel subLabel = new JLabel(subMessage);
            subLabel.setForeground(UiConstants.TEXT_SECONDARY);
            subLabel.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
            subLabel.setBounds(PADDING_X, 96, WIDTH - PADDING_X * 2, 20);
            content.add(subLabel);
        }

        // 按钮行:次要描边按钮左、主渐变按钮右,底部右对齐;单按钮模式主按钮居中
        int buttonsY = (singleButton ? HEIGHT_COMPACT : HEIGHT) - BUTTON_BOTTOM_GAP - BUTTON_HEIGHT;
        if (!singleButton) {
            JButton keepButton = new OutlineButton(keepText);
            keepButton.setBounds(WIDTH - PADDING_X - BUTTON_WIDTH * 2 - BUTTON_GAP, buttonsY, BUTTON_WIDTH,
                    BUTTON_HEIGHT);
            keepButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            content.add(keepButton);
        }

        JButton confirmButton = new GradientButton(confirmText);
        if (singleButton) {
            confirmButton.setBounds((WIDTH - BUTTON_WIDTH) / 2, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT);
        } else {
            confirmButton.setBounds(WIDTH - PADDING_X - BUTTON_WIDTH, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
        confirmButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                dispose();
            }
        });
        content.add(confirmButton);

        // ESC等价于保留(次要按钮)
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                CLOSE_ACTION_KEY);
        content.getActionMap().put(CLOSE_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setContentPane(content);
        // 根面板不透明会兜底涂色,同样置透明
        getRootPane().setOpaque(false);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), ARC, ARC));
        }
    }

    /**
     * 弹出主题化确认弹窗并阻塞等待用户选择
     * @param owner 父窗口
     * @param title 标题
     * @param message 主文案
     * @param subMessage 副文案
     * @param confirmText 确认按钮文本
     * @param keepText 次要按钮文本,为null或空串时单按钮模式(主按钮居中)
     * @return true=点击确认按钮,false=次要按钮/ESC/关闭
     */
    public static boolean show(Window owner, String title, String message, String subMessage,
            String confirmText, String keepText) {
        ThemedConfirmDialog dialog = new ThemedConfirmDialog(owner, title, message, subMessage, confirmText, keepText);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    /**
     * 弹出单按钮提示弹窗(无返回值,OK即关闭),用于纯提示场景
     * @param owner 父窗口
     * @param title 标题
     * @param message 主文案
     * @param okText 按钮文本
     */
    public static void showMessage(Window owner, String title, String message, String okText) {
        new ThemedConfirmDialog(owner, title, message, "", okText, null).setVisible(true);
    }

    /**
     * 次要描边按钮:透明底+主题亮色描边,悬停轻微提亮
     */
    private static class OutlineButton extends JButton {
        /**
         * 是否悬停
         */
        private boolean hovered;

        OutlineButton(String text) {
            super(text);
            setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.BODY_FONT_SIZE));
            setForeground(UiConstants.TEXT_PRIMARY);
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
            if (hovered) {
                g2d.setColor(new Color(255, 255, 255, 24));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), OUTLINE_ARC, OUTLINE_ARC);
            }
            g2d.setColor(UiConstants.ACCENT_BRIGHT);
            g2d.setStroke(new BasicStroke(OUTLINE_STROKE));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, OUTLINE_ARC, OUTLINE_ARC);
            g2d.dispose();
            super.paintComponent(g);
        }
    }
}
