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

import com.gxl.encryptdog.gui.application.dto.OperationProgressDTO;
import com.gxl.encryptdog.gui.application.dto.OperationResultDTO;
import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;
import com.gxl.encryptdog.utils.Utils;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * 执行表格页:第一层操作标题(操作中→操作完成,白色、字号与首页标题一致),
 * 第二层四个统计小窗(文件数量/成功数/失败数/总耗时),
 * 第三层9列自绘文件列表(半透明圆角行条、主题色小表头、紫色渐变进度条)+ Back按钮。
 * 执行完成停留本页,结果合并回填。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/27 14:00
 */
public class ProgressPanel extends JPanel {
    /**
     * 默认字体键
     */
    private static final String   DEFAULT_FONT_KEY = "defaultFont";
    /**
     * 内容内边距
     */
    private static final int      PADDING          = 28;
    /**
     * 统计小窗间距
     */
    private static final int      CARD_GAP         = 16;
    /**
     * 统计小窗高度(紧凑)
     */
    private static final int      STAT_CARD_HEIGHT = 54;
    /**
     * 行高
     */
    private static final int      ROW_H            = 46;
    /**
     * 行间垂直间距
     */
    private static final int      ROW_GAP          = 6;
    /**
     * 列表表头高度
     */
    private static final int      HEADER_H         = 30;
    /**
     * 列表列标题
     */
    private static final String[] COLUMNS          = { "No", "Source File", "Before Size", "After Size", "State",
            "Progress", "Estimated Time", "Target File", "Result" };
    /**
     * 列表列宽比(总和1151,按面板可用宽度等比缩放;进度列加宽承载马赛克进度条与百分比文本,状态列加宽容纳胶囊徽章,Source/Target收敛让出空间)
     */
    private static final int[]    COLUMN_RATIOS    = { 36, 195, 80, 80, 92, 283, 80, 195, 110 };
    /**
     * 列表列宽比总和
     */
    private static final int      RATIO_TOTAL      = 1151;
    /**
     * 状态文本(与核心EncryptStateEnum定义的状态一致)
     */
    private static final String   STATUS_WAITING   = "WAITING";
    /**
     * 状态文本(与核心EncryptStateEnum定义的状态一致)
     */
    private static final String   STATUS_RUNNING   = "RUNNING";
    /**
     * 状态文本(与核心EncryptStateEnum定义的状态一致)
     */
    private static final String   STATUS_DONE      = "FINISHED";
    /**
     * 操作开始时间
     */
    private long                  beginMs;
    /**
     * 是否已进入完成/失败终态
     */
    private volatile boolean      finished;
    /**
     * 内窗顶部状态小字(算法信息,失败时展示错误信息)
     */
    private final JLabel          statusLabel      = new JLabel(" ");
    /**
     * 统计小窗
     */
    private final StatCard        statOperation    = new StatCard("Operation");
    /**
     * 统计小窗
     */
    private final StatCard        statFiles        = new StatCard("Files");
    /**
     * 统计小窗
     */
    private final StatCard        statSuccess      = new StatCard("Success");
    /**
     * 统计小窗
     */
    private final StatCard        statFailed       = new StatCard("Failed");
    /**
     * 统计小窗
     */
    private final StatCard        statElapsed      = new StatCard("Elapsed");
    /**
     * 第三层:列表表头(带主题背景色)
     */
    private final JPanel          headerPanel      = new HeaderBand();
    /**
     * 第三层:行容器
     */
    private final JPanel          rowsPanel        = new JPanel();
    /**
     * 列表滚动容器
     */
    private final JScrollPane     scrollPane;
    /**
     * 顶部隐形拖拽区起点(窗口标题栏隐藏时仍可拖动)
     */
    private Point                 dragOffset;
    /**
     * 当前操作类型文本(Encrypt/Decrypt)
     */
    private String                operationName     = "Encrypt";
    /**
     * 行数据快照
     */
    private final List<RowData>   rows             = new ArrayList<>();
    /**
     * 马赛克脉冲动画相位
     */
    private float                 phase;
    /**
     * 马赛克脉冲动画时钟(33ms,仅执行页停留期间运行)
     */
    private final Timer           pulseTimer;

    public ProgressPanel(ActionListener backListener) {
        setOpaque(false);
        setLayout(null);

        statusLabel.setForeground(UiConstants.TEXT_SECONDARY);
        statusLabel.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
        add(statusLabel);

        // 五个统计小窗(操作类型、文件数量、成功数、失败数、总耗时)
        add(statOperation);
        add(statFiles);
        add(statSuccess);
        add(statFailed);
        add(statElapsed);

        // 第三层:表头(带主题背景色)+ 行容器
        headerPanel.setLayout(null);
        add(headerPanel);

        rowsPanel.setOpaque(false);
        rowsPanel.setLayout(null);
        scrollPane = new JScrollPane(rowsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        // 仅垂直滚动,水平滚动条不显示(列宽随面板等比缩放,无水平溢出)
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(ROW_H + ROW_GAP);
        add(scrollPane);

        // 马赛克脉冲动画时钟:推进共享相位与各行缓动追赶,并仅重绘行区,所有行进度条共用
        pulseTimer = new Timer(33, e -> {
            phase += 0.14F;
            for (var row : rows) {
                row.bar.tick();
            }
            rowsPanel.repaint();
        });

        // 顶部隐形拖拽区:窗口标题栏在执行页隐藏,顶部34px仍可拖动窗口
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.getY() < 34) {
                    dragOffset = e.getPoint();
                }
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (dragOffset == null) {
                    return;
                }
                java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(ProgressPanel.this);
                if (window != null) {
                    java.awt.Point locationOnScreen = e.getLocationOnScreen();
                    window.setLocation(locationOnScreen.x - dragOffset.x, locationOnScreen.y - dragOffset.y);
                }
            }
        });
    }

    /**
     * 面板脱离卡片(返回表单/窗口关闭)时停止脉冲动画,避免表单页空转
     */
    @Override
    public void removeNotify() {
        pulseTimer.stop();
        super.removeNotify();
    }

    /**
     * 进入执行表格页:清空旧行,立即展示标题、统计小窗与全部文件行占位
     * @param operation 操作类型,ENCRYPT/DECRYPT
     * @param algorithm 加密算法
     * @param files 源文件列表
     */
    public void begin(String operation, String algorithm, List<String> files) {
        beginMs = System.currentTimeMillis();
        finished = false;
        pulseTimer.start();
        // 清空上一轮操作残留的行数据(修复重复行bug)
        rows.clear();
        operationName = "ENCRYPT".equals(operation) ? "Encrypt" : "Decrypt";
        statusLabel.setText(operationName + " · " + algorithm);
        statusLabel.setForeground(UiConstants.TEXT_SECONDARY);
        statOperation.setValue(operationName);
        statFiles.setValue(String.valueOf(files.size()));
        statSuccess.setValue("0");
        statFailed.setValue("0");
        statElapsed.setValue("00:00");
        int no = 1;
        for (var file : files) {
            rows.add(new RowData(no++, file, "-", "-", STATUS_WAITING, "-", "-", "", "-"));
        }
        rebuildRows();
    }

    /**
     * 刷新进度快照
     * @param progress
     */
    public void refresh(OperationProgressDTO progress) {
        if (finished) {
            return;
        }
        // 按源文件路径原地更新:保留已有行及其进度条实例(动画状态不丢失),新增缺失行、移除消失行
        var bySource = new HashMap<String, RowData>();
        for (var row : rows) {
            bySource.put(row.source, row);
        }
        rows.clear();
        int success = 0;
        int failed = 0;
        for (var fp : progress.getFileProgressList()) {
            if ("SUCCESS".equals(fp.getResult())) {
                success++;
            } else if ("FAILED".equals(fp.getResult())) {
                failed++;
            }
            var row = bySource.get(fp.getSourceFile());
            if (row == null) {
                row = new RowData(0, fp.getSourceFile(), fp.getSourceFileSize(), fp.getTargetFileSize(),
                        resolveStatus(fp), fp.getProgress(), fp.getEstimatedTime(), fp.getTargetFile(),
                        "-".equals(fp.getResult()) ? "-" : fp.getResult());
            } else {
                row.no = 0;
                row.before = fp.getSourceFileSize();
                row.after = fp.getTargetFileSize();
                row.state = resolveStatus(fp);
                row.progress = fp.getProgress();
                row.eta = fp.getEstimatedTime();
                row.target = fp.getTargetFile();
                row.result = "-".equals(fp.getResult()) ? "-" : fp.getResult();
                // 目标进度更新,缓动动画自动追赶
                row.bar.setTarget(parsePercent(row.progress));
            }
            rows.add(row);
        }
        // 按状态排序:进行中 > 等待中 > 已完成,序号随排序重排
        rows.sort((a, b) -> Integer.compare(statePriority(a.state), statePriority(b.state)));
        int no = 1;
        for (var row : rows) {
            row.no = no++;
        }
        statSuccess.setValue(String.valueOf(success));
        statFailed.setValue(String.valueOf(failed));
        statElapsed.setValue(Utils.currentTimeFormat(elapsedSeconds()));
        rebuildRows();
    }

    /**
     * 状态排序优先级:进行中0 > 等待中1 > 已完成2
     * @param state
     * @return
     */
    private static int statePriority(String state) {
        if (STATUS_RUNNING.equals(state)) {
            return 0;
        }
        if (STATUS_WAITING.equals(state)) {
            return 1;
        }
        return 2;
    }

    /**
     * 执行完成:合并最终结果并停留本页
     * @param result
     */
    public void finish(OperationResultDTO result) {
        finished = true;
        for (var row : rows) {
            for (var fr : result.getFileResults()) {
                if (fr.getSourceFile().equals(row.source)) {
                    row.after = fr.getTargetFileSize();
                    row.state = STATUS_DONE;
                    // 失败行进度条停留在失败时刻的进度,不强制到100%
                    if ("SUCCESS".equals(fr.getResult())) {
                        row.progress = "100%";
                    }
                    row.target = fr.getTargetFile();
                    row.result = fr.getResult();
                    row.errorMsg = fr.getErrorMsg();
                    // 目标进度更新(成功100%平滑填满/失败停留),缓动动画追赶
                    row.bar.setTarget(parsePercent(row.progress));
                    break;
                }
            }
        }
        statusLabel.setText(" ");
        statFiles.setValue(String.valueOf(result.getTotalFiles()));
        statSuccess.setValue(String.valueOf(result.getSuccessCount()));
        statFailed.setValue(String.valueOf(result.getFailedCount()));
        statElapsed.setValue(result.getTimeConsuming());
        rebuildRows();
    }

    /**
     * 执行异常:错误信息展示在标题下并停留本页
     * @param message
     */
    public void showFailure(String message) {
        finished = true;
        // 错误详情以红色展示在内容区顶部
        statusLabel.setForeground(UiConstants.ERROR_RED);
        statusLabel.setText(message);
    }

    /**
     * 由文件进度推导状态
     * @param fp
     * @return
     */
    private String resolveStatus(OperationProgressDTO.FileProgress fp) {
        // 优先取核心枚举透传的状态,缺失时回退推导(兼容未带状态的快照来源)
        if (fp.getState() != null && !fp.getState().isBlank()) {
            return fp.getState();
        }
        if ("SUCCESS".equals(fp.getResult()) || "FAILED".equals(fp.getResult())) {
            return STATUS_DONE;
        }
        if (fp.getProgress() == null || fp.getProgress().isBlank() || "-".equals(fp.getProgress())) {
            return STATUS_WAITING;
        }
        return STATUS_RUNNING;
    }

    /**
     * 状态展示映射:首字母大写、其余小写(仅作用于单元格展示,排序与配色仍按原始值判断)
     * @param state 内部状态值
     * @return 展示文本
     */
    private static String displayState(String state) {
        if (state == null || state.isBlank()) {
            return state;
        }
        return state.substring(0, 1).toUpperCase(Locale.ROOT) + state.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * 百分比归一化:未产生进度(进度为 -/空白)时显示 0%(等待行与失败前的行),其余原样展示
     * @param progress 进度文本
     * @return 展示文本
     */
    private static String resolvePercent(String progress) {
        if (progress == null || progress.isBlank() || "-".equals(progress)) {
            return "0%";
        }
        return progress;
    }

    /**
     * 已执行秒数
     * @return
     */
    private long elapsedSeconds() {
        return Math.max(0, (System.currentTimeMillis() - beginMs) / 1000);
    }

    /**
     * 当前列x坐标与宽度
     * @return [x0,w0,x1,w1,...]
     */
    private int[] columnLayout() {
        int available = getWidth() - PADDING * 2;
        int[] layout = new int[COLUMNS.length * 2];
        int x = 0;
        for (int i = 0; i < COLUMNS.length; i++) {
            int w = available * COLUMN_RATIOS[i] / RATIO_TOTAL;
            layout[i * 2] = x;
            layout[i * 2 + 1] = w;
            x += w;
        }
        return layout;
    }

    /**
     * 重建表头与行视图
     */
    private void rebuildRows() {
        int[] layout = columnLayout();
        // 表头
        headerPanel.removeAll();
        for (int i = 0; i < COLUMNS.length; i++) {
            JLabel head = new JLabel(COLUMNS[i]);
            head.setForeground(UiConstants.TEXT_PRIMARY);
            head.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.BOLD, 11F));
            // 表头文字全部居中对齐
            head.setHorizontalAlignment(SwingConstants.CENTER);
            head.setBounds(layout[i * 2], 0, layout[i * 2 + 1], HEADER_H);
            headerPanel.add(head);
        }
        // 行
        rowsPanel.removeAll();
        int rowsHeight = rows.size() * (ROW_H + ROW_GAP) + 8;
        rowsPanel.setBounds(0, 0, getWidth() - PADDING * 2, rowsHeight);
        // null布局面板首选尺寸缺省为0,JScrollPane视口不会滚动,必须显式设置
        rowsPanel.setPreferredSize(new java.awt.Dimension(getWidth() - PADDING * 2, rowsHeight));
        int y = 0;
        for (var data : rows) {
            rowsPanel.add(buildRow(data, layout, y));
            y += ROW_H + ROW_GAP;
        }
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    /**
     * 构建单行(半透明圆角行条 + 9列内容)
     * @param data 行数据
     * @param layout 列布局
     * @param y 行y
     * @return
     */
    private JPanel buildRow(RowData data, int[] layout, int y) {
        JPanel row = new RowStrip();
        row.setBounds(0, y, getWidth() - PADDING * 2, ROW_H);

        JLabel noLabel = cellLabel(String.valueOf(data.no), UiConstants.TEXT_SECONDARY, SwingConstants.CENTER, layout, 0);
        JLabel sourceLabel = cellLabel(shearToFit(data.source, layout[3], UIManager.getFont(DEFAULT_FONT_KEY)
                .deriveFont(Font.PLAIN, 12F)), UiConstants.TEXT_PRIMARY, SwingConstants.LEFT, layout, 1);
        sourceLabel.setToolTipText(data.source);
        JLabel beforeLabel = cellLabel(data.before, UiConstants.TEXT_SECONDARY, SwingConstants.CENTER, layout, 2);
        JLabel afterLabel = cellLabel(data.after, UiConstants.TEXT_SECONDARY, SwingConstants.CENTER, layout, 3);

        // 状态胶囊徽章:实例随行复用,状态随快照更新,绘制时在单元格内自适应居中
        StateChip chip = data.chip;
        chip.setState(data.state);
        chip.setBounds(layout[8] + 6, 0, layout[9] - 12, ROW_H);

        // 复用行内进度条实例(动画状态随行保留),固定253×12(照搬示例数值),百分比标签紧贴条右端
        MosaicBar bar = data.bar;
        bar.setBounds(layout[10] + 8, (ROW_H - 12) / 2, 253, 12);
        row.add(bar);
        // 进度条右侧百分比文本(紧贴条右端;未产生进度显示0%)
        JLabel percentLabel = new JLabel(resolvePercent(data.progress));
        percentLabel.setForeground(UiConstants.TEXT_SECONDARY);
        percentLabel.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, 12F));
        percentLabel.setHorizontalAlignment(SwingConstants.LEFT);
        percentLabel.setBounds(layout[10] + 265, 0, 50, ROW_H);
        row.add(percentLabel);

        JLabel etaLabel = cellLabel(data.eta, UiConstants.TEXT_SECONDARY, SwingConstants.CENTER, layout, 6);
        JLabel targetLabel = cellLabel(shearToFit(data.target, layout[15], UIManager.getFont(DEFAULT_FONT_KEY)
                .deriveFont(Font.PLAIN, 12F)), UiConstants.TEXT_PRIMARY, SwingConstants.LEFT, layout, 7);
        targetLabel.setToolTipText(data.target);

        // 结果列:成功/失败以图标呈现(替代文字),失败图标悬停显示失败原因
        JLabel resultLabel;
        if ("SUCCESS".equals(data.result)) {
            resultLabel = iconCell(LogoUtil.loadImage(UiConstants.RESULT_SUCCESS_RESOURCE, 14), layout, 8);
        } else if ("FAILED".equals(data.result)) {
            resultLabel = iconCell(LogoUtil.loadImage(UiConstants.RESULT_ERROR_RESOURCE, 14), layout, 8);
            resultLabel.setToolTipText(data.errorMsg);
        } else {
            resultLabel = cellLabel("-", UiConstants.TEXT_SECONDARY, SwingConstants.CENTER, layout, 8);
        }

        row.add(noLabel);
        row.add(sourceLabel);
        row.add(beforeLabel);
        row.add(afterLabel);
        row.add(chip);
        row.add(etaLabel);
        row.add(targetLabel);
        row.add(resultLabel);
        return row;
    }

    /**
     * 构建单元格标签
     * @param text 文本
     * @param color 颜色
     * @param align 对齐
     * @param layout 列布局
     * @param column 列索引
     * @return
     */
    private JLabel cellLabel(String text, Color color, int align, int[] layout, int column) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, 12F));
        label.setHorizontalAlignment(align);
        label.setBounds(layout[column * 2] + 6, 0, layout[column * 2 + 1] - 12, ROW_H);
        return label;
    }

    /**
     * 构建居中图标单元格
     * @param icon 图标
     * @param layout 列布局
     * @param column 列索引
     * @return
     */
    private JLabel iconCell(javax.swing.Icon icon, int[] layout, int column) {
        JLabel label = new JLabel(icon);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(layout[column * 2] + 6, 0, layout[column * 2 + 1] - 12, ROW_H);
        return label;
    }

    /**
     * 终端风格截断:超出可用宽度时丢弃前段、前缀 "...",末尾字符完整显示
     * @param text 原文
     * @param width 可用宽度
     * @param font 字体
     * @return 截断后文本
     */
    private String shearToFit(String text, int width, Font font) {
        if (text == null) {
            return "";
        }
        FontMetrics metrics = getFontMetrics(font);
        if (metrics.stringWidth(text) <= width - 12) {
            return text;
        }
        String prefix = "...";
        String result = text;
        while (result.length() > 1 && metrics.stringWidth(prefix + result) > width - 12) {
            result = result.substring(1);
        }
        return prefix + result;
    }

    /**
     * 面板尺寸确定后重算子组件布局;面板未挂载时跳过(窗口形态切换时面板脱离卡片)
     * @param x
     * @param y
     * @param w
     * @param h
     */
    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (isShowing()) {
            layoutContent();
        }
    }

    /**
     * 按当前面板尺寸重排内容
     */
    void layoutContent() {
        int w = getWidth();
        int h = getHeight();
        // 状态文字(算法信息/失败错误)顶部小字 + 五个统计小窗(横向等宽铺满)+ 表头 + 行滚动区(整体上移减少留白)
        statusLabel.setBounds(PADDING, 10, w - PADDING * 2, 18);
        int statY = 32;
        int cardW = (w - PADDING * 2 - CARD_GAP * 4) / 5;
        StatCard[] cards = { statOperation, statFiles, statSuccess, statFailed, statElapsed };
        for (int i = 0; i < cards.length; i++) {
            cards[i].setBounds(PADDING + i * (cardW + CARD_GAP), statY, cardW, STAT_CARD_HEIGHT);
        }
        int listY = statY + STAT_CARD_HEIGHT + 10;
        headerPanel.setBounds(PADDING, listY, w - PADDING * 2, HEADER_H);
        scrollPane.setBounds(PADDING, listY + HEADER_H + 4, w - PADDING * 2, h - listY - HEADER_H - 20);
        if (!rows.isEmpty()) {
            rebuildRows();
        }
    }

    /**
     * 行数据快照
     */
    private final class RowData {
        /**
         * 序号(排序后重排)
         */
        private int          no;
        /**
         * 源文件
         */
        private final String source;
        /**
         * 处理前大小
         */
        private String       before;
        /**
         * 处理后大小
         */
        private String       after;
        /**
         * 状态
         */
        private String       state;
        /**
         * 进度
         */
        private String       progress;
        /**
         * 预计时间
         */
        private String       eta;
        /**
         * 目标文件
         */
        private String       target;
        /**
         * 处理结果
         */
        private String       result;
        /**
         * 失败原因
         */
        private String       errorMsg;
        /**
         * 行内进度条实例(跨快照复用,保留缓动动画状态)
         */
        private final MosaicBar bar;
        /**
         * 状态胶囊徽章实例(跨快照复用,呼吸动画状态由共享相位推导)
         */
        private final StateChip chip;

        RowData(int no, String source, String before, String after, String state, String progress, String eta,
                String target, String result) {
            this.no = no;
            this.source = source;
            this.before = before;
            this.after = after;
            this.state = state;
            this.progress = progress;
            this.eta = eta;
            this.target = target;
            this.result = result;
            this.bar = new MosaicBar(parsePercent(progress));
            this.chip = new StateChip(state);
        }
    }

    /**
     * 表头背景:主色系半透明深紫(四色渐变紫段)圆角条
     */
    private static final class HeaderBand extends JPanel {
        /**
         * 圆角半径
         */
        private static final int ARC = 12;

        HeaderBand() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(UiConstants.MOSAIC_PUR.getRed(), UiConstants.MOSAIC_PUR.getGreen(),
                    UiConstants.MOSAIC_PUR.getBlue(), 150));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2d.dispose();
        }
    }

    /**
     * 行条:与主题同源的半透明圆角样式,无网格
     */
    private static final class RowStrip extends JPanel {
        /**
         * 圆角半径
         */
        private static final int ARC = 12;

        RowStrip() {
            setOpaque(false);
            setLayout(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(255, 255, 255, 10));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2d.dispose();
        }
    }

    /**
     * 状态胶囊徽章:圆角矩形低透明度状态色底色+同色描边+状态色文字,
     * 配色为主题同源阶梯(Waiting灰→Running紫→Finished薰衣草白),三态均静态渲染
     */
    private final class StateChip extends JComponent {
        /**
         * 徽章高度(行高内垂直居中)
         */
        private static final int CHIP_H        = 22;
        /**
         * 文字水平内边距
         */
        private static final int TEXT_PAD      = 10;
        /**
         * 当前状态(WAITING/RUNNING/FINISHED)
         */
        private String           state;

        StateChip(String state) {
            this.state = state;
            setOpaque(false);
        }

        /**
         * 更新状态
         * @param state 内部状态值
         */
        void setState(String state) {
            this.state = state;
        }

        /**
         * 状态主题色:Waiting灰/Running紫/Finished薰衣草白(主题同源阶梯:沉睡→活跃→归于平静)
         * @return 对应主题色
         */
        private Color stateColor() {
            if (STATUS_RUNNING.equals(state)) {
                return UiConstants.ACCENT_BRIGHT;
            }
            if (STATUS_WAITING.equals(state)) {
                return UiConstants.TEXT_SECONDARY;
            }
            return UiConstants.TEXT_PRIMARY;
        }

        /**
         * 底色alpha:三态静态(Waiting 15%/Running 20%/Finished 20%)
         * @return 0-255
         */
        private int fillAlpha() {
            return STATUS_WAITING.equals(state) ? 38 : 51;
        }

        /**
         * 描边alpha:Waiting 40%,Running/Finished 50%
         * @return 0-255
         */
        private int borderAlpha() {
            return STATUS_WAITING.equals(state) ? 102 : 128;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (state == null || state.isBlank()) {
                return;
            }
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String text = displayState(state);
            Font font = UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.BOLD, 11F);
            g2d.setFont(font);
            FontMetrics metrics = g2d.getFontMetrics();
            // 宽=实测文字宽+内边距(收缩至列宽内),高固定,圆角=高/2全胶囊,单元格内水平垂直居中
            int w = Math.max(1, Math.min(metrics.stringWidth(text) + TEXT_PAD * 2, getWidth()));
            int h = CHIP_H;
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            Color base = stateColor();
            // 低透明度状态色底色
            g2d.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), fillAlpha()));
            g2d.fillRoundRect(x, y, w, h, h, h);
            // 1px同色描边
            g2d.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), borderAlpha()));
            g2d.setStroke(new BasicStroke(1F));
            g2d.drawRoundRect(x, y, w, h, h, h);
            // 状态色文字(全饱和度)
            g2d.setColor(base);
            int ty = y + (h - metrics.getHeight()) / 2 + metrics.getAscent();
            g2d.drawString(text, x + (w - metrics.stringWidth(text)) / 2, ty);
            g2d.dispose();
        }
    }

    /**
     * 四色马赛克进度条:乳白→灰→淡紫→紫渐变方格、每格确定性相位明灭脉冲、圆角深色轨道、填充随进度
     */
    private final class MosaicBar extends JComponent {
        /**
         * 四色渐变采样级数
         */
        private static final int RAMP_LEVELS = 8;
        /**
         * 快照目标进度(0-100)
         */
        private float target;
        /**
         * 当前显示进度(0-100,缓动追赶target)
         */
        private float display;

        MosaicBar(int percent) {
            this.target = percent;
            this.display = percent;
            setOpaque(false);
        }

        /**
         * 更新目标进度(快照变化时调用)
         * @param percent
         */
        void setTarget(int percent) {
            this.target = percent;
        }

        /**
         * 缓动追赶一帧:指数平滑先快后慢,差值小于0.5直接归位避免永动
         */
        void tick() {
            if (Math.abs(target - display) < 0.5F) {
                display = target;
                return;
            }
            display += (target - display) * 0.12F;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int height = getHeight();
            // 轨道固定253px宽(照搬示例TRACK_END-SLIDER_X=253),自左端起
            int trackW = 253;
            RoundRectangle2D track = new RoundRectangle2D.Double(0, 0, trackW, height, height, height);
            g2d.setColor(UiConstants.MOSAIC_TRACK);
            g2d.fill(track);
            if (display <= 0) {
                g2d.dispose();
                return;
            }
            // 马赛克方格:边长=条高/2、1px缝,裁剪于圆角轨道内保持圆角,仅绘制填充区内方格
            Color[] ramp = fourColorRamp();
            int cell = height / 2;
            int rows = height / cell;
            int fillEnd = (int) ((double) display / 100 * trackW);
            Shape oldClip = g2d.getClip();
            g2d.setClip(track);
            Object oldAA = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            for (int cx = 0; cx < fillEnd; cx += cell) {
                double t = (double) cx / trackW;
                for (int row = 0; row < rows; row++) {
                    // 每格独立确定性相位围绕四色渐变位置明灭,不闪烁
                    double pulse = Math.sin(phase + hash(cx / cell, row) * Math.PI * 2);
                    int lv = (int) Math.round(t * (RAMP_LEVELS - 1) + pulse * 2.0);
                    lv = Math.max(0, Math.min(RAMP_LEVELS - 1, lv));
                    g2d.setColor(ramp[lv]);
                    int w = Math.min(cell - 1, fillEnd - cx);
                    g2d.fillRect(cx, row * cell, w, cell - 1);
                }
            }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
            g2d.setClip(oldClip);
            g2d.dispose();
        }
    }

    /**
     * 四色渐变采样:乳白→灰→淡紫→紫,乳白只占最左一小段
     * @return 8级色阶
     */
    private static Color[] fourColorRamp() {
        Color[] stops = { UiConstants.MOSAIC_MILK, UiConstants.MOSAIC_GRAY, UiConstants.MOSAIC_LPUR, UiConstants.MOSAIC_PUR };
        float[] pos = { 0F, 0.12F, 0.40F, 0.70F };
        Color[] ramp = new Color[8];
        for (int i = 0; i < ramp.length; i++) {
            double t = i / 7.0;
            int s = (t <= pos[1]) ? 0 : (t <= pos[2]) ? 1 : 2;
            double tt = (t - pos[s]) / (pos[s + 1] - pos[s]);
            ramp[i] = lerpColor(stops[s], stops[s + 1], Math.max(0, Math.min(1, tt)));
        }
        return ramp;
    }

    /**
     * 颜色线性插值
     * @param a 起始色
     * @param b 结束色
     * @param t 比例
     * @return 插值色
     */
    private static Color lerpColor(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /**
     * 确定性伪随机(保证格子明灭相位稳定不闪烁)
     * @param x 列索引
     * @param y 行索引
     * @return 0..1
     */
    private static double hash(int x, int y) {
        double s = Math.sin(x * 127.1 + y * 311.7) * 43758.5453;
        return s - Math.floor(s);
    }

    /**
     * 统计小窗:半透明白圆角小窗,数值与名称居中
     */
    private static final class StatCard extends JPanel {
        /**
         * 圆角半径
         */
        private static final int    ARC        = 14;
        /**
         * 默认字体键
         */
        private static final String FONT_KEY   = "defaultFont";
        /**
         * 数值标签
         */
        private final JLabel        valueLabel = new JLabel("0");
        /**
         * 名称标签
         */
        private final JLabel        nameLabel;

        StatCard(String name) {
            setOpaque(false);
            setLayout(null);
            valueLabel.setForeground(UiConstants.TEXT_PRIMARY);
            valueLabel.setFont(UIManager.getFont(FONT_KEY).deriveFont(Font.BOLD, 18F));
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(valueLabel);
            nameLabel = new JLabel(name);
            nameLabel.setForeground(UiConstants.TEXT_SECONDARY);
            nameLabel.setFont(UIManager.getFont(FONT_KEY).deriveFont(Font.PLAIN, 11F));
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(nameLabel);
        }

        void setValue(String value) {
            valueLabel.setText(value);
        }

        @Override
        public void setBounds(int x, int y, int w, int h) {
            super.setBounds(x, y, w, h);
            valueLabel.setBounds(14, 4, w - 28, 26);
            nameLabel.setBounds(14, 32, w - 28, 15);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(255, 255, 255, 16));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2d.setColor(new Color(255, 255, 255, 36));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2d.dispose();
        }
    }

    /**
     * 解析进度百分比文本
     * @param text
     * @return
     */
    private static int parsePercent(String text) {
        if (text == null || text.isBlank() || "-".equals(text)) {
            return 0;
        }
        try {
            return (int) Double.parseDouble(text.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
