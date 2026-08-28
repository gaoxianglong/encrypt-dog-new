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

import com.gxl.encryptdog.gui.application.dto.EncryptFormDTO;
import com.gxl.encryptdog.gui.application.dto.OperationListener;
import com.gxl.encryptdog.gui.application.dto.OperationProgressDTO;
import com.gxl.encryptdog.gui.application.dto.OperationResultDTO;
import com.gxl.encryptdog.gui.application.error.GuiAnchor;
import com.gxl.encryptdog.gui.application.error.GuiException;
import com.gxl.encryptdog.gui.application.service.EncryptOperationAppService;
import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * 无边框主窗口：自绘标题栏 + 粒子层 + 毛玻璃卡片，
 * 编排表单→确认→进度→结果三态切换与成功爆发/失败抖动动画。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class EncryptDogFrame extends JFrame {
    /**
     * 内容区高度
     */
    private static final int     CONTENT_HEIGHT = UiConstants.WINDOW_HEIGHT - UiConstants.TITLE_BAR_HEIGHT;
    /**
     * 卡片 X 坐标（内容区居中）
     */
    private static final int     CARD_X         = (UiConstants.WINDOW_WIDTH - UiConstants.CARD_WIDTH) / 2;
    /**
     * 卡片 Y 坐标（整窗居中,上下边距80/80）
     * 修正说明:原按内容区居中(CARD_Y=57),标题栏46px造成视觉下沉23px,
     * 故在内容区居中基础上再上移半个标题栏高度
     */
    private static final int     CARD_Y         = (CONTENT_HEIGHT - UiConstants.CARD_HEIGHT) / 2 - UiConstants.TITLE_BAR_HEIGHT / 2;
    /**
     * 执行页窗口宽(16:10长方形)
     */
    private static final int     WIDE_WIDTH           = 1400;
    /**
     * 执行页窗口高(加高以容纳约10行文件列表)
     */
    private static final int     WIDE_HEIGHT          = 880;
    /**
     * 宽屏卡片左右边距
     */
    private static final int     WIDE_CARD_X          = 20;
    /**
     * 宽屏卡片上边距(蒙层顶与home图标底保留约12px间隙)
     */
    private static final int     WIDE_CARD_Y          = 34;
    /**
     * 宽屏卡片底边与内容区底部的间距(确保不遮挡版权信息)
     */
    private static final int     WIDE_CARD_BOTTOM_GAP = 44;
    /**
     * 应用服务
     */
    private final EncryptOperationAppService appService = new EncryptOperationAppService();
    /**
     * 粒子背景层
     */
    private final ParticlePanel  particlePanel;
    /**
     * 毛玻璃卡片
     */
    private final GlassCardPanel glassCard;
    /**
     * 表单面板
     */
    private final EncryptFormPanel formPanel;
    /**
     * 执行表格面板
     */
    private final ProgressPanel  progressPanel;
    /**
     * 分层面板(窗口形态动画时重排)
     */
    private final JLayeredPane   layeredPane;
    /**
     * 底部版权信息(随窗口形态重定位)
     */
    private final JLabel         copyrightLabel       = new JLabel("Copyright (c) 2021-2031 gaoxianglong");
    /**
     * 窗口顶部标题栏(所有页面常显,不做任何改动)
     */
    private final TitleBar       titleBar;
    /**
     * 执行页返回图标(紧贴标题栏下方,仅执行页展示)
     */
    private final JButton        homeButton          = new JButton();
    /**
     * 是否处于动画/执行过渡中（防止重复提交）
     */
    private volatile boolean     transitioning;

    public EncryptDogFrame() {
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(UiConstants.WINDOW_WIDTH, UiConstants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        titleBar = new TitleBar(this);
        add(titleBar, BorderLayout.NORTH);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(UiConstants.WINDOW_WIDTH, CONTENT_HEIGHT));

        particlePanel = new ParticlePanel();
        particlePanel.setBounds(0, 0, UiConstants.WINDOW_WIDTH, CONTENT_HEIGHT);
        layeredPane.add(particlePanel, JLayeredPane.DEFAULT_LAYER);

        // 底部版权信息:粒子层之上、卡片下方居中展示(窗口y730 = 分层面板y684)
        copyrightLabel.setForeground(UiConstants.TEXT_SECONDARY);
        copyrightLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
        copyrightLabel.setHorizontalAlignment(JLabel.CENTER);
        copyrightLabel.setBounds(0, CONTENT_HEIGHT - 30, UiConstants.WINDOW_WIDTH, 20);
        // PALETTE层: 与粒子面板跨层,JLayeredPane才会跟踪重叠区域并正确重绘;
        // 同层(默认层)下optimized drawing会假定不重叠,粒子60fps全幅重绘会每帧盖掉标签
        layeredPane.add(copyrightLabel, JLayeredPane.PALETTE_LAYER);

        // 执行页返回图标(紧贴窗口标题栏下方,仅执行页展示)
        homeButton.setIcon(LogoUtil.loadImage(UiConstants.BACK_RESOURCE, 20));
        homeButton.setOpaque(false);
        homeButton.setContentAreaFilled(false);
        homeButton.setBorderPainted(false);
        homeButton.setFocusPainted(false);
        homeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        homeButton.setToolTipText("Back to form");
        // 紧贴窗口标题栏底部(2px间隙)
        homeButton.setBounds(12, 2, 20, 20);
        homeButton.setVisible(false);
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backToForm();
            }
        });
        layeredPane.add(homeButton, JLayeredPane.PALETTE_LAYER);

        glassCard = new GlassCardPanel();
        glassCard.setBounds(CARD_X, CARD_Y, UiConstants.CARD_WIDTH, UiConstants.CARD_HEIGHT);
        layeredPane.add(glassCard, JLayeredPane.PALETTE_LAYER);

        formPanel = new EncryptFormPanel(new EncryptFormPanel.FormListener() {
            @Override
            public void onSubmit(EncryptFormDTO form) {
                handleSubmit(form);
            }
        });
        formPanel.setBounds(0, 0, UiConstants.CARD_WIDTH, UiConstants.CARD_HEIGHT);
        glassCard.add(formPanel);

        progressPanel = new ProgressPanel(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backToForm();
            }
        });
        progressPanel.setBounds(0, 0, UiConstants.CARD_WIDTH, UiConstants.CARD_HEIGHT);

        add(layeredPane, BorderLayout.CENTER);

        forwardCardMouseToParticles(glassCard);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            applyRoundedShape();
        }
    }

    @Override
    public void dispose() {
        particlePanel.stop();
        super.dispose();
    }

    /**
     * 预填表单（来自--gui的剩余命令行参数）
     * @param form
     */
    public void prefill(EncryptFormDTO form) {
        formPanel.prefill(form);
    }

    /**
     * 处理表单提交：校验 → 解析 → 确认 → 执行
     * @param form
     */
    private void handleSubmit(EncryptFormDTO form) {
        if (transitioning) {
            return;
        }
        try {
            // 校验 + 解析文件列表
            var files = appService.prepareOperation(form);
            // 删除源文件二次确认（等价终端第二次Y/N）
            if (form.isDelete() && !confirmDeleteDialog(form, files)) {
                form.setDelete(false);
            }
            startOperation(form, files);
        } catch (GuiException e) {
            formPanel.showError(e.getMessage(), e.getAnchor());
        }
    }

    /**
     * 删除源文件二次确认弹窗（主题化:仅提示后果与文件数量,不列出文件清单）
     * @param form
     * @param files 解析后的源文件列表
     * @return
     */
    private boolean confirmDeleteDialog(EncryptFormDTO form, List<String> files) {
        var action = form.isEncrypt() ? "encryption" : "decryption";
        var countText = files.size() == 1 ? "1 source file will be deleted." : files.size() + " source files will be deleted.";
        return ThemedConfirmDialog.show(this, "Delete source files",
                "After " + action + ", " + countText,
                "Choosing Keep files only cancels the deletion. The operation continues.",
                "Confirm delete", "Keep files");
    }

    /**
     * 后台线程执行加/解密操作
     * @param form
     * @param files
     */
    private void startOperation(EncryptFormDTO form, List<String> files) {
        transitioning = true;
        formPanel.clearError();
        // 窗口标题栏在所有页面保持展示(用户明确不动它);执行页仅展示home返回图标
        homeButton.setVisible(true);
        // 直接切换宽屏形态与执行表格页(无逐帧动画与淡入,避免卡顿)
        switchWindowShape(true);
        glassCard.removeAll();
        glassCard.add(progressPanel);
        progressPanel.setBounds(0, 0, glassCard.getWidth(), glassCard.getHeight());
        glassCard.revalidate();
        glassCard.repaint();
        progressPanel.begin(form.isEncrypt() ? "ENCRYPT" : "DECRYPT", form.getEncryptAlgorithm(), files);
        // 进度回调在调度线程触发,统一切换到EDT刷新
        var listener = new OperationListener() {
            @Override
            public void onProgress(final OperationProgressDTO progress) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        progressPanel.refresh(progress);
                    }
                });
            }
        };
        // 工作线程执行阻塞调用,避免卡住EDT
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    var result = appService.execute(form, files, listener);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            finishOperation(result);
                        }
                    });
                } catch (final GuiException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            operationFailed(e.getMessage());
                        }
                    });
                }
            }
        }, "encrypt-dog-gui-worker");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * 操作完成:最终结果合并进执行表格并停留本页(完成提示音由core的FinishedListener播放)
     * @param result
     */
    private void finishOperation(OperationResultDTO result) {
        progressPanel.finish(result);
        transitioning = false;
    }

    /**
     * 操作失败:停留执行表格页,错误信息展示在汇总区
     * @param message
     */
    private void operationFailed(String message) {
        progressPanel.showFailure(message);
        transitioning = false;
    }

    /**
     * 再来一次:回到表单
     */
    private void backToForm() {
        if (transitioning) {
            return;
        }
        transitioning = true;
        // 隐藏执行页home图标
        homeButton.setVisible(false);
        // 直接换回表单(无淡入动画),窗口直接切回方形
        glassCard.removeAll();
        glassCard.add(formPanel);
        glassCard.revalidate();
        glassCard.repaint();
        // 完成后返回表单,全量重置输入缓存
        formPanel.reset();
        switchWindowShape(false);
        transitioning = false;
    }

    /**
     * 窗口形态直接切换(无逐帧动画,避免卡顿):方形与16:10长方形之间一步到位,保持屏幕居中、圆角与内容同步
     * @param toWide true=宽屏,false=方形
     */
    private void switchWindowShape(boolean toWide) {
        int w = toWide ? WIDE_WIDTH : UiConstants.WINDOW_WIDTH;
        int h = toWide ? WIDE_HEIGHT : UiConstants.WINDOW_HEIGHT;
        Point screenCenter = new Point(getLocation().x + getWidth() / 2, getLocation().y + getHeight() / 2);
        setSize(w, h);
        // 保持屏幕中心不动
        setLocation(screenCenter.x - w / 2, screenCenter.y - h / 2);
        applyRoundedShape();
        relayoutContent(w, h, true);
    }

    /**
     * 窗口形态变化时重排内容层(粒子层/版权信息/卡片)。
     * 动画期间仅改bounds不触发全树校验,结束帧统一revalidate一次,避免逐帧卡顿。
     * @param w 窗口宽
     * @param h 窗口高
     * @param settle 是否动画结束(触发revalidate)
     */
    private void relayoutContent(int w, int h, boolean settle) {
        // 窗口标题栏在所有页面常显,内容区位于其下方
        int contentH = h - UiConstants.TITLE_BAR_HEIGHT;
        particlePanel.setBounds(0, 0, w, contentH);
        copyrightLabel.setBounds(0, contentH - 30, w, 20);
        // home图标:紧贴窗口标题栏底部(2px间隙)
        homeButton.setLocation(12, 2);
        if (w > UiConstants.WINDOW_WIDTH) {
            // 宽屏:卡片铺满内容区,底边保持在版权信息上方
            glassCard.setBounds(WIDE_CARD_X, WIDE_CARD_Y, w - WIDE_CARD_X * 2, contentH - WIDE_CARD_Y - WIDE_CARD_BOTTOM_GAP);
        } else {
            glassCard.setBounds(CARD_X, CARD_Y, UiConstants.CARD_WIDTH, UiConstants.CARD_HEIGHT);
        }
        progressPanel.setBounds(0, 0, glassCard.getWidth(), glassCard.getHeight());
        if (settle) {
            layeredPane.revalidate();
            layeredPane.repaint();
        }
    }

    /**
     * 将卡片上的鼠标位置转发给粒子层（保证全窗口鼠标微扰）。
     * @param source 鼠标事件来源组件
     */
    private void forwardCardMouseToParticles(GlassCardPanel source) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point point = SwingUtilities.convertPoint(source, e.getPoint(), particlePanel);
                particlePanel.setMousePoint(point);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                particlePanel.setMousePoint(null);
            }
        };
        source.addMouseListener(mouseAdapter);
        source.addMouseMotionListener(mouseAdapter);
    }

    /**
     * 应用整窗圆角形状。
     */
    private void applyRoundedShape() {
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                UiConstants.WINDOW_ARC, UiConstants.WINDOW_ARC));
    }
}
