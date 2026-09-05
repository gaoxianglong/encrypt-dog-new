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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * 自绘标题栏：主题背景（渐变顶部色）+ 应用名称 + 最小化/关闭按钮，支持按住空白处拖拽窗口。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class TitleBar extends JPanel {
    /**
     * 窗口按钮边长
     */
    private static final int    BUTTON_SIZE        = 32;
    /**
     * 窗口按钮字号
     */
    private static final int    BUTTON_FONT_SIZE   = 14;
    /**
     * 标题左内边距
     */
    private static final int    TITLE_PADDING_LEFT = 14;
    /**
     * 按钮间水平间距
     */
    private static final int    BUTTON_GAP         = 2;
    /**
     * 按钮容器上边距
     */
    private static final int    BUTTON_BOX_TOP     = 7;
    /**
     * 按钮容器右边距
     */
    private static final int    BUTTON_BOX_RIGHT   = 8;
    /**
     * 悬停圆底内缩量
     */
    private static final int    HOVER_INSET        = 4;
    /**
     * 最小化按钮符号
     */
    private static final String MINIMIZE_GLYPH     = "—";
    /**
     * 关闭按钮符号
     */
    private static final String CLOSE_GLYPH        = "✕";
    /**
     * 默认字体键
     */
    private static final String DEFAULT_FONT_KEY   = "defaultFont";
    /**
     * 拖拽起点（相对标题栏）
     */
    private Point               dragOffset;
    /**
     * 所属窗口
     */
    private final JFrame        frame;

    /**
     * 构造标题栏。
     *
     * @param frame 所属窗口
     */
    public TitleBar(JFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(UiConstants.WINDOW_WIDTH, UiConstants.TITLE_BAR_HEIGHT));
        setLayout(new BorderLayout());
        setOpaque(false);

        // 标题区:logo小图标 + 应用名称,BoxLayout保证内容在标题栏高度内垂直居中
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.X_AXIS));
        titleBox.setBorder(BorderFactory.createEmptyBorder(0, TITLE_PADDING_LEFT, 0, 0));
        JLabel titleIconLabel = new JLabel(LogoUtil.loadTitleLogo(UiConstants.LOGO_TITLE_SIZE));
        // 显式18x18尺寸约束,ImageIcon按label尺寸缩放显示
        titleIconLabel.setPreferredSize(new Dimension(UiConstants.LOGO_TITLE_SIZE, UiConstants.LOGO_TITLE_SIZE));
        titleIconLabel.setMaximumSize(new Dimension(UiConstants.LOGO_TITLE_SIZE, UiConstants.LOGO_TITLE_SIZE));
        titleBox.add(titleIconLabel);
        titleBox.add(Box.createHorizontalStrut(6));
        JLabel titleLabel = new JLabel(UiConstants.APP_NAME);
        titleLabel.setForeground(UiConstants.TEXT_PRIMARY);
        // 字号与logo同高平行
        titleLabel.setFont(UIManager.getFont(DEFAULT_FONT_KEY)
                .deriveFont(Font.BOLD, UiConstants.TITLE_BAR_FONT_SIZE));
        titleBox.add(titleLabel);
        add(titleBox, BorderLayout.WEST);

        JPanel buttonBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, BUTTON_GAP, BUTTON_BOX_TOP));
        buttonBox.setOpaque(false);
        buttonBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, BUTTON_BOX_RIGHT));
        buttonBox.add(new WindowButton(MINIMIZE_GLYPH, false));
        buttonBox.add(new WindowButton(CLOSE_GLYPH, true));
        add(buttonBox, BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset == null) {
                    return;
                }
                Point locationOnScreen = e.getLocationOnScreen();
                frame.setLocation(locationOnScreen.x - dragOffset.x, locationOnScreen.y - dragOffset.y);
            }
        });
    }

    /**
     * 绘制标题栏背景：填充主题背景渐变顶部色，与内容区粒子渐变顶部无缝衔接
     * （保持setOpaque(false)，显式填充避免开启opaque后Swing优化绘制路径变化）。
     *
     * @param g 绘图上下文
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(UiConstants.BG_TOP);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    /**
     * 标题栏窗口按钮：最小化（—）与关闭（✕），自绘悬停圆底。
     */
    private final class WindowButton extends JButton {
        /**
         * 是否悬停
         */
        private boolean       hovered;
        /**
         * 是否为关闭按钮
         */
        private final boolean closeButton;

        /**
         * 构造窗口按钮。
         *
         * @param glyph       按钮符号
         * @param closeButton 是否为关闭按钮
         */
        private WindowButton(String glyph, boolean closeButton) {
            super(glyph);
            this.closeButton = closeButton;
            setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
            setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, BUTTON_FONT_SIZE));
            setForeground(UiConstants.TEXT_SECONDARY);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    setForeground(UiConstants.BUTTON_TEXT);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    setForeground(UiConstants.TEXT_SECONDARY);
                    repaint();
                }
            });
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleClick();
                }
            });
        }

        /**
         * 处理按钮点击：关闭或最小化窗口。托盘模式下✕仅隐藏窗口(进程常驻,任务继续),
         * 无托盘时保持退出语义(降级矩阵)。
         */
        private void handleClick() {
            if (closeButton) {
                if (frame.getDefaultCloseOperation() == WindowConstants.HIDE_ON_CLOSE) {
                    frame.setVisible(false);
                } else {
                    frame.dispose();
                }
            } else {
                frame.setState(JFrame.ICONIFIED);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hovered) {
                g2d.setColor(closeButton ? UiConstants.TITLE_BAR_CLOSE_HOVER : UiConstants.TITLE_BAR_HOVER);
                g2d.fillOval(HOVER_INSET, HOVER_INSET,
                        getWidth() - HOVER_INSET * 2, getHeight() - HOVER_INSET * 2);
            }
            g2d.dispose();
            super.paintComponent(g);
        }
    }
}
