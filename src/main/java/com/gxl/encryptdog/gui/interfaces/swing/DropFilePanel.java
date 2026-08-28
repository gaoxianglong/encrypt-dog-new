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
import lombok.extern.slf4j.Slf4j;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 文件拖拽框:深色圆角底 + 虚线边框;空态居中展示文件图标与英文提示,有文件时展示滚动列表。
 * 支持拖入悬停边框呼吸脉冲、放下渐变涟漪、新行依次淡入三层主题动画。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/26 15:50
 */
@Slf4j
public class DropFilePanel extends JPanel {
    /**
     * 默认字体键
     */
    private static final String            DEFAULT_FONT_KEY  = "defaultFont";
    /**
     * 圆角半径
     */
    private static final int               ARC_RADIUS        = 10;
    /**
     * 悬停脉冲周期(毫秒)
     */
    private static final int               PULSE_PERIOD_MS   = 1200;
    /**
     * 涟漪时长(毫秒)
     */
    private static final int               RIPPLE_DURATION_MS = 600;
    /**
     * 行淡入步进间隔(毫秒)
     */
    private static final int               FADE_STEP_MS      = 80;
    /**
     * 行淡入每步进度增量
     */
    private static final float             FADE_STEP_DELTA   = 0.2F;
    /**
     * 涟漪扫过带宽(像素)
     */
    private static final int               RIPPLE_BAND_WIDTH = 120;
    /**
     * 涟漪最大透明度
     */
    private static final int               RIPPLE_MAX_ALPHA  = 110;
    /**
     * 空态提示文案
     */
    private static final String            EMPTY_HINT        = "Drag files or folders here";
    /**
     * 列表选中行淡紫背景(半透明ACCENT,与满饱和ACCENT组件区分)
     */
    private static final Color             LIST_SELECTION_BG = new Color(108, 99, 255, 80);
    /**
     * 文件列表模型
     */
    private final DefaultListModel<String> fileListModel;
    /**
     * 文件列表
     */
    private final JList<String>            fileList;
    /**
     * 拖拽回调
     */
    private final DropFileListener         dropListener;
    /**
     * 是否拖入悬停中
     */
    private boolean                        dragOver;
    /**
     * 脉冲相位(弧度)
     */
    private float                          pulsePhase;
    /**
     * 悬停脉冲计时器
     */
    private final Timer                    pulseTimer;
    /**
     * 涟漪进度,负数表示未激活
     */
    private float                          rippleProgress     = -1F;
    /**
     * 涟漪开始时间
     */
    private long                           rippleStartMs;
    /**
     * 涟漪计时器
     */
    private final Timer                    rippleTimer;
    /**
     * 行淡入进度表(行索引 -> 0~1)
     */
    private final Map<Integer, Float>      fadeProgress       = new HashMap<>();
    /**
     * 待淡入行队列
     */
    private final List<Integer>            pendingFadeRows    = new ArrayList<>();
    /**
     * 行淡入计时器
     */
    private final Timer                    fadeTimer;

    /**
     * 构造拖拽框
     * @param fileListModel 外部共享的文件列表模型
     * @param dropListener  拖拽回调
     */
    public DropFilePanel(DefaultListModel<String> fileListModel, DropFileListener dropListener) {
        this.fileListModel = fileListModel;
        this.dropListener = dropListener;
        setOpaque(false);
        setLayout(new BorderLayout());

        fileList = new JList<>(fileListModel);
        fileList.setOpaque(false);
        fileList.setBackground(new Color(0, 0, 0, 0));
        fileList.setForeground(UiConstants.TEXT_PRIMARY);
        // 选中行淡紫背景,与滚动条/分段选择器等ACCENT组件区分
        fileList.setSelectionBackground(LIST_SELECTION_BG);
        fileList.setSelectionForeground(UiConstants.TEXT_PRIMARY);
        fileList.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
        fileList.setCellRenderer(new FadeCellRenderer());
        // 列表只读,不支持拖出
        fileList.setDragEnabled(false);

        // 悬停脉冲
        pulseTimer = new Timer(UiConstants.FRAME_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pulsePhase += (float) (Math.PI * 2 * UiConstants.FRAME_DELAY_MS / PULSE_PERIOD_MS);
                repaint();
            }
        });
        // 放下涟漪
        rippleTimer = new Timer(UiConstants.FRAME_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rippleProgress = (float) (System.currentTimeMillis() - rippleStartMs) / RIPPLE_DURATION_MS;
                if (rippleProgress >= 1F) {
                    rippleProgress = -1F;
                    rippleTimer.stop();
                }
                repaint();
            }
        });
        // 行淡入
        fadeTimer = new Timer(FADE_STEP_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                advanceFade();
            }
        });

        // 拖拽目标:面板与列表各自独立创建与绑定,共享同一监听逻辑
        DropTargetListener dndListener = new DndListener();
        setDropTarget(new DropTarget(this, dndListener));
        fileList.setDropTarget(new DropTarget(fileList, dndListener));

        // 模型变化驱动空态/列表切换与行淡入
        fileListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                onModelChanged();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                onModelChanged();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                onModelChanged();
            }
        });
        onModelChanged();
    }

    /**
     * 模型变化处理:切换空态/列表视图,新行加入淡入队列
     */
    private void onModelChanged() {
        boolean empty = fileListModel.isEmpty();
        removeAll();
        if (!empty) {
            // 列表视图:JScrollPane承载,文件多时可滚动(AS_NEEDED细滚动条,FlatLaf主题样式)
            fileList.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
            JScrollPane scrollPane = new JScrollPane(fileList);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(null);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            add(scrollPane, BorderLayout.CENTER);
            // 新行进入淡入队列
            for (int i = 0; i < fileListModel.size(); i++) {
                if (!fadeProgress.containsKey(i)) {
                    fadeProgress.put(i, 0F);
                    pendingFadeRows.add(i);
                }
            }
            if (!pendingFadeRows.isEmpty() && !fadeTimer.isRunning()) {
                fadeTimer.start();
            }
        } else {
            // 空态:清空淡入状态
            fadeProgress.clear();
            pendingFadeRows.clear();
            fadeTimer.stop();
        }
        revalidate();
        repaint();
    }

    /**
     * 推进行淡入:每步推进一个待淡入行的进度
     */
    private void advanceFade() {
        while (!pendingFadeRows.isEmpty()) {
            Integer row = pendingFadeRows.get(0);
            Float progress = fadeProgress.get(row);
            if (Objects.isNull(progress)) {
                pendingFadeRows.remove(0);
                continue;
            }
            float next = Math.min(1F, progress + FADE_STEP_DELTA);
            fadeProgress.put(row, next);
            if (next >= 1F) {
                pendingFadeRows.remove(0);
            }
            break;
        }
        if (pendingFadeRows.isEmpty()) {
            fadeTimer.stop();
        }
        fileList.repaint();
    }

    /**
     * 启动悬停脉冲
     */
    private void startPulse() {
        if (!dragOver) {
            dragOver = true;
            pulsePhase = 0F;
            pulseTimer.start();
            repaint();
        }
    }

    /**
     * 停止悬停脉冲
     */
    private void stopPulse() {
        if (dragOver) {
            dragOver = false;
            pulseTimer.stop();
            repaint();
        }
    }

    /**
     * 启动放下涟漪
     */
    private void startRipple() {
        rippleProgress = 0F;
        rippleStartMs = System.currentTimeMillis();
        if (!rippleTimer.isRunning()) {
            rippleTimer.start();
        }
        repaint();
    }

    /**
     * 移除选中文件
     */
    public void removeSelectedFiles() {
        int[] indices = fileList.getSelectedIndices();
        // 倒序移除避免索引偏移
        for (int i = indices.length - 1; i >= 0; i--) {
            fileListModel.remove(indices[i]);
            fadeProgress.remove(indices[i]);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 深色圆角底
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(0, 0, width, height, ARC_RADIUS, ARC_RADIUS);

        // 虚线边框(悬停时ACCENT呼吸)
        Color borderColor = dragOver ? pulseBorderColor() : new Color(255, 255, 255, 60);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.5F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10F, new float[] { 8F, 6F }, 0F));
        g2d.drawRoundRect(0, 0, width - 1, height - 1, ARC_RADIUS, ARC_RADIUS);

        // 空态:居中文件图标 + 英文提示
        if (fileListModel.isEmpty()) {
            var icon = LogoUtil.loadImage(UiConstants.FILE_EMPTY_RESOURCE, UiConstants.FILE_EMPTY_ICON_SIZE);
            if (Objects.nonNull(icon)) {
                icon.paintIcon(this, g2d, (width - UiConstants.FILE_EMPTY_ICON_SIZE) / 2,
                        (height - UiConstants.FILE_EMPTY_ICON_SIZE) / 2 - 12);
            }
            g2d.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
            g2d.setColor(UiConstants.TEXT_SECONDARY);
            FontMetrics metrics = g2d.getFontMetrics();
            int textX = (width - metrics.stringWidth(EMPTY_HINT)) / 2;
            int textY = height / 2 + UiConstants.FILE_EMPTY_ICON_SIZE / 2 + 4;
            g2d.drawString(EMPTY_HINT, textX, textY);
        }

        // 放下涟漪:ACCENT渐变条带水平扫过
        if (rippleProgress >= 0F) {
            int sweepX = (int) (rippleProgress * (width + RIPPLE_BAND_WIDTH * 2)) - RIPPLE_BAND_WIDTH;
            g2d.setPaint(new GradientPaint(sweepX, 0, new Color(108, 99, 255, 0),
                    sweepX + RIPPLE_BAND_WIDTH, 0, new Color(108, 99, 255, RIPPLE_MAX_ALPHA)));
            g2d.fillRect(sweepX, 0, RIPPLE_BAND_WIDTH, height);
        }
        g2d.dispose();
    }

    /**
     * 悬停脉冲边框颜色:ACCENT与ACCENT_BRIGHT间正弦振荡(整体亮度区间提升)
     * @return
     */
    private Color pulseBorderColor() {
        float ratio = (float) ((Math.sin(pulsePhase) + 1.0) / 2.0);
        int red = (int) (UiConstants.ACCENT.getRed()
                + (UiConstants.ACCENT_BRIGHT.getRed() - UiConstants.ACCENT.getRed()) * ratio);
        int green = (int) (UiConstants.ACCENT.getGreen()
                + (UiConstants.ACCENT_BRIGHT.getGreen() - UiConstants.ACCENT.getGreen()) * ratio);
        int blue = (int) (UiConstants.ACCENT.getBlue()
                + (UiConstants.ACCENT_BRIGHT.getBlue() - UiConstants.ACCENT.getBlue()) * ratio);
        return new Color(red, green, blue);
    }

    /**
     * 拖拽回调接口
     */
    public interface DropFileListener {
        /**
         * 文件拖入回调
         * @param files 拖入的文件与文件夹
         */
        void onFilesDropped(List<File> files);
    }

    /**
     * 行淡入渲染器:委托默认渲染后,选中行显式绘制淡紫背景,新行按行索引叠加前景alpha
     */
    private final class FadeCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!(component instanceof JComponent)) {
                return component;
            }
            JComponent jc = (JComponent) component;
            if (isSelected) {
                // 非opaque列表下显式绘制选中背景(半透明淡紫)
                jc.setBackground(LIST_SELECTION_BG);
                jc.setOpaque(true);
            } else {
                jc.setOpaque(false);
                // 仅对新行做前景淡入,选中行直接显示
                Float progress = fadeProgress.get(index);
                if (Objects.nonNull(progress) && progress < 1F) {
                    Color base = UiConstants.TEXT_PRIMARY;
                    int alpha = (int) (255 * progress);
                    jc.setForeground(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
                }
            }
            return component;
        }
    }

    /**
     * 拖拽监听:悬停脉冲、放下处理与涟漪
     */
    private final class DndListener implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                // macOS Finder在dragEnter阶段报告的action常为ACTION_NONE,必须显式COPY
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
                startPulse();
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            stopPulse();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void drop(DropTargetDropEvent dtde) {
            stopPulse();
            if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                // rejectDrop仅在acceptDrop之前合法
                dtde.rejectDrop();
                return;
            }
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            boolean success = false;
            try {
                List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (Objects.isNull(files) || files.isEmpty()) {
                    // macOS上flavor支持不代表数据可取,粘贴板可能没有文件URL
                    log.warn("Drop data unavailable, current data flavors: {}", dtde.getCurrentDataFlavorsAsList());
                } else {
                    dropListener.onFilesDropped(files);
                    startRipple();
                    success = true;
                }
            } catch (Exception e) {
                log.warn("Drop failed, current data flavors: {}", dtde.getCurrentDataFlavorsAsList(), e);
            } finally {
                // acceptDrop之后唯一合法收尾是dropComplete,否则原生拖拽会话会卡死
                dtde.dropComplete(success);
            }
        }
    }
}
