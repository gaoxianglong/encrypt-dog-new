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
import com.gxl.encryptdog.gui.application.error.GuiAnchor;
import com.gxl.encryptdog.gui.interfaces.swing.constant.UiConstants;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 加密狗表单面板：模式切换、源文件、密钥、算法、目标目录与选项，
 * 字段与终端命令参数一一对应。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class EncryptFormPanel extends JPanel {
    /**
     * 左侧标签宽度
     */
    private static final int     LABEL_WIDTH        = 100;
    /**
     * 左侧标签X
     */
    private static final int     LABEL_X            = 30;
    /**
     * 输入区X
     */
    private static final int     FIELD_X            = 134;
    /**
     * 输入区宽度
     */
    private static final int     FIELD_WIDTH        = 420;
    /**
     * 默认字体键
     */
    private static final String  DEFAULT_FONT_KEY   = "defaultFont";
    /**
     * 加密按钮文本
     */
    private static final String  ENCRYPT_TEXT       = "Encrypt";
    /**
     * 解密按钮文本
     */
    private static final String  DECRYPT_TEXT       = "Decrypt";
    /**
     * 相邻字段行距(确认密钥行行高),解密模式重排偏移量
     */
    private static final int     ROW_PITCH          = 46;
    /**
     * 错误气泡与锚点字段的间距
     */
    private static final int     TOAST_ANCHOR_GAP   = 8;
    /**
     * 表单提交回调
     */
    private final FormListener   listener;
    /**
     * 加密模式切换按钮
     */
    /**
     * 加解密模式等宽分段选择器
     */
    private final SegmentedToggle modeToggle        = new SegmentedToggle(new String[] { "Encrypt", "Decrypt" });
    /**
     * 已选文件列表模型
     */
    private final DefaultListModel<String> fileListModel = new DefaultListModel<>();
    /**
     * 文件拖拽框
     */
    private final DropFilePanel   dropPanel;
    /**
     * 密钥输入框
     */
    private final PasswordToggleField secretKeyField  = new PasswordToggleField();
    /**
     * 确认密钥输入框,仅加密模式显示。
     * 约定:位于本行下方的任何新增控件必须登记到layoutRows,否则解密模式会出现布局空洞
     */
    private final PasswordToggleField confirmKeyField = new PasswordToggleField();
    /**
     * 确认密钥标签,仅加密模式显示。
     * 约定:位于本行下方的任何新增控件必须登记到layoutRows,否则解密模式会出现布局空洞
     */
    private final JLabel              confirmKeyLabel = new JLabel("Confirm key");
    /**
     * 算法下拉框,坐标由layoutRows统一管理
     */
    private final JComboBox<String> algorithmCombo  = new JComboBox<>(new String[] { "AES", "DESede", "XOR" });
    /**
     * 算法标签,坐标由layoutRows统一管理
     */
    private final JLabel              algorithmLabel;
    /**
     * 目标目录标签,坐标由layoutRows统一管理
     */
    private final JLabel              targetDirLabel;
    /**
     * 目标目录输入组(字段+Browse按钮),坐标由layoutRows统一管理
     */
    private final JPanel              targetBox;
    /**
     * 目标目录输入框
     */
    private final JTextField     targetPathField    = new JTextField();
    /**
     * 删除源文件复选框
     */
    private final JCheckBox      deleteCheckBox     = new JCheckBox("Delete source files after operation");
    /**
     * 仅限本机复选框
     */
    private final JCheckBox      onlyLocalCheckBox  = new JCheckBox("Local machine only (-o highest security)");
    /**
     * XOR算法安全提示,仅加密模式且选中XOR时显示
     */
    private final JLabel         xorWarningLabel    = new JLabel("Use with caution");
    /**
     * 主操作按钮
     */
    private final GradientButton submitButton;
    /**
     * 字段内联错误提示,显示在出错字段内部,约2秒逐步淡出
     */
    private final FieldHint      fieldHint          = new FieldHint();

    public EncryptFormPanel(FormListener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(null);

        // 主副标题已取消:模式切换栏为蒙层首行,左边缘与下方字段标签列对齐,右边缘与输入区一致
        modeToggle.setBounds(LABEL_X, 14, FIELD_X + FIELD_WIDTH - LABEL_X, 36);
        modeToggle.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                switchMode(modeToggle.getSelectedIndex() == 0);
            }
        });
        add(modeToggle);

        dropPanel = new DropFilePanel(fileListModel, new DropFilePanel.DropFileListener() {
            @Override
            public void onFilesDropped(List<File> files) {
                for (var file : files) {
                    addFilePath(file.getAbsolutePath());
                }
            }
        });
        // 拖拽区左边缘与字段标签列对齐,右边缘与输入区一致
        dropPanel.setBounds(LABEL_X, 54, FIELD_X + FIELD_WIDTH - LABEL_X, 112);
        add(dropPanel);

        // 按钮行:选择文件/选择目录左对齐,删除选中右对齐
        JButton selectFileButton = createSmallButton("+ Select files");
        selectFileButton.setBounds(FIELD_X, 170, 130, 26);
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFiles(false);
            }
        });
        add(selectFileButton);

        JButton selectDirButton = createSmallButton("+ Select directory");
        selectDirButton.setBounds(FIELD_X + 142, 170, 130, 26);
        selectDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFiles(true);
            }
        });
        add(selectDirButton);

        JButton removeButton = createSmallButton("Remove selected");
        removeButton.setBounds(FIELD_X + FIELD_WIDTH - 130, 170, 130, 26);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dropPanel.removeSelectedFiles();
            }
        });
        add(removeButton);

        // 密钥区
        addLabel("Secret key", 206);
        secretKeyField.setBounds(FIELD_X, 202, FIELD_WIDTH, UiConstants.INPUT_HEIGHT);
        add(secretKeyField);

        confirmKeyLabel.setBounds(LABEL_X, 256, LABEL_WIDTH, 22);
        styleLabel(confirmKeyLabel);
        add(confirmKeyLabel);
        confirmKeyField.setBounds(FIELD_X, 248, FIELD_WIDTH, UiConstants.INPUT_HEIGHT);
        add(confirmKeyField);

        // 算法区,坐标由layoutRows统一管理
        algorithmLabel = addLabel("Algorithm", 302);
        algorithmCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateXorWarning();
                }
            }
        });
        add(algorithmCombo);

        // XOR安全提示,仅加密模式且选中XOR时显示,与下拉框同高垂直居中,坐标由layoutRows统一管理
        xorWarningLabel.setForeground(UiConstants.TEXT_SECONDARY);
        xorWarningLabel.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
        xorWarningLabel.setVerticalAlignment(JLabel.CENTER);
        add(xorWarningLabel);

        // 目标目录区: 字段420全宽,Browse按钮内嵌右端(与密码框眼睛按钮同模式),坐标由layoutRows统一管理
        targetDirLabel = addLabel("Target directory", 348);
        targetBox = new JPanel(new BorderLayout());
        targetBox.setOpaque(false);
        targetPathField.setToolTipText("Leave blank to use the source directory");
        targetBox.add(targetPathField, BorderLayout.CENTER);
        JButton browseButton = createSmallButton("Browse…");
        browseButton.setPreferredSize(new Dimension(98, UiConstants.INPUT_HEIGHT));
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseTargetDir();
            }
        });
        targetBox.add(browseButton, BorderLayout.EAST);
        add(targetBox);

        // 选项区,坐标由layoutRows统一管理
        styleCheckBox(deleteCheckBox);
        add(deleteCheckBox);
        styleCheckBox(onlyLocalCheckBox);
        add(onlyLocalCheckBox);

        // 字段内联错误提示,位置在showError时按锚点字段定位;
        // 显式置顶(z序索引0为最上层),确保提示不被下方字段内容遮挡
        add(fieldHint);
        setComponentZOrder(fieldHint, 0);

        // 主按钮,坐标由layoutRows统一管理
        submitButton = new GradientButton(ENCRYPT_TEXT);

        // 版本号:卡片右下角,低调展示不抢占视觉焦点
        JLabel versionLabel = new JLabel(UiConstants.VERSION);
        versionLabel.setForeground(UiConstants.TEXT_SECONDARY);
        versionLabel.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, 11F));
        versionLabel.setHorizontalAlignment(JLabel.RIGHT);
        versionLabel.setBounds(UiConstants.CARD_WIDTH - 150, 578, 134, 14);
        add(versionLabel);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });
        add(submitButton);

        // 缺省为加密模式
        switchMode(true);
    }

    /**
     * 表单提交回调
     */
    public interface FormListener {
        /**
         * 提交表单
         * @param form 表单数据
         */
        void onSubmit(EncryptFormDTO form);
    }

    /**
     * 切换加/解密模式,驱动确认密钥框显隐、下方模块布局与主按钮文字
     * @param isEncrypt
     */
    private void switchMode(boolean isEncrypt) {
        modeToggle.setSelectedIndex(isEncrypt ? 0 : 1);
        // 确认密钥框仅加密模式显示,对齐终端"解密不二次确认"语义
        confirmKeyLabel.setVisible(isEncrypt);
        confirmKeyField.setVisible(isEncrypt);
        submitButton.setText(isEncrypt ? ENCRYPT_TEXT : DECRYPT_TEXT);
        // 确认密钥行隐藏时下方模块整体上移一个行距,消除空洞
        layoutRows(isEncrypt);
        // XOR安全提示随模式联动
        updateXorWarning();
        revalidate();
        repaint();
    }

    /**
     * 集中管理确认密钥行下方全部模块的坐标。
     * 解密模式下确认密钥行隐藏,下方模块整体上移一个行距(ROW_PITCH),消除布局空洞。
     * 约定:位于确认密钥行下方的任何新增控件都必须登记到本方法,否则解密模式会出现空洞。
     * @param isEncrypt 是否加密模式
     */
    private void layoutRows(boolean isEncrypt) {
        int offset = isEncrypt ? 0 : ROW_PITCH;
        algorithmLabel.setBounds(LABEL_X, 302 - offset, LABEL_WIDTH, 24);
        algorithmCombo.setBounds(FIELD_X, 294 - offset, 180, UiConstants.INPUT_HEIGHT);
        xorWarningLabel.setBounds(322, 294 - offset, 232, UiConstants.INPUT_HEIGHT);
        targetDirLabel.setBounds(LABEL_X, 348 - offset, LABEL_WIDTH, 24);
        targetBox.setBounds(FIELD_X, 340 - offset, FIELD_WIDTH, UiConstants.INPUT_HEIGHT);
        deleteCheckBox.setBounds(FIELD_X, 386 - offset, 260, 26);
        onlyLocalCheckBox.setBounds(FIELD_X, 416 - offset, 300, 26);
        submitButton.setBounds((UiConstants.CARD_WIDTH - 260) / 2, 472 - offset, 260, UiConstants.PRIMARY_BUTTON_HEIGHT);
    }

    /**
     * 添加左侧字段标签,返回标签引用(确认密钥行下方的标签需保存引用并登记到layoutRows)
     * @param text
     * @param y
     * @return 创建的标签
     */
    private JLabel addLabel(String text, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(LABEL_X, y, LABEL_WIDTH, 24);
        styleLabel(label);
        add(label);
        return label;
    }

    /**
     * 渲染标签样式
     * @param label
     */
    private void styleLabel(JLabel label) {
        label.setForeground(UiConstants.TEXT_SECONDARY);
        label.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.BODY_FONT_SIZE));
    }

    /**
     * 渲染复选框样式
     * @param checkBox
     */
    private void styleCheckBox(JCheckBox checkBox) {
        checkBox.setOpaque(false);
        checkBox.setForeground(UiConstants.TEXT_SECONDARY);
        checkBox.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.BODY_FONT_SIZE));
    }

    /**
     * 创建小按钮
     * @param text
     * @return
     */
    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(UiConstants.ACCENT_BRIGHT);
        button.setFont(UIManager.getFont(DEFAULT_FONT_KEY).deriveFont(Font.PLAIN, UiConstants.SMALL_FONT_SIZE));
        return button;
    }

    /**
     * 选择文件或目录
     * @param directoryOnly 是否仅选择目录
     */
    private void chooseFiles(boolean directoryOnly) {
        JFileChooser chooser = new JFileChooser();
        if (directoryOnly) {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(true);
        }
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (directoryOnly) {
            addFilePath(chooser.getSelectedFile().getAbsolutePath());
        } else {
            for (var file : chooser.getSelectedFiles()) {
                addFilePath(file.getAbsolutePath());
            }
        }
    }

    /**
     * 添加文件路径到列表,去重
     * @param path
     */
    private void addFilePath(String path) {
        if (!fileListModel.contains(path)) {
            fileListModel.addElement(path);
        }
    }

    /**
     * 更新XOR安全提示显隐:仅加密模式且选中XOR时显示
     */
    private void updateXorWarning() {
        xorWarningLabel.setVisible(isEncryptMode() && "XOR".equals(algorithmCombo.getSelectedItem()));
    }

    /**
     * 选择目标输出目录
     */
    private void chooseTargetDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            targetPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * 展示内联错误提示:显示在出错字段内部(主题色),约2秒逐步淡出
     * @param message
     * @param anchor 错误锚点字段
     */
    public void showError(String message, GuiAnchor anchor) {
        fieldHint.showHint(message);
        JComponent target = anchorComponent(anchor);
        var metrics = fieldHint.getFontMetrics(fieldHint.getFont());
        int hintWidth = Math.min(metrics.stringWidth(message) + FieldHint.HINT_PADDING_X * 2,
                hintMaxWidth(anchor, target));
        int hintHeight = Math.min(26, target.getHeight() - 6);
        fieldHint.setSize(hintWidth, hintHeight);
        if (anchor == GuiAnchor.FILE) {
            // 拖拽区顶部条带居中:避开空态图片与提示文案(位于拖拽区垂直中部)
            fieldHint.setLocation(target.getX() + (target.getWidth() - hintWidth) / 2, target.getY() + 6);
        } else if (anchor == GuiAnchor.SYSTEM) {
            // 无字段错误:主按钮上方居中
            fieldHint.setLocation((UiConstants.CARD_WIDTH - hintWidth) / 2,
                    submitButton.getY() - hintHeight - TOAST_ANCHOR_GAP);
        } else {
            // 字段内部左侧,垂直居中
            fieldHint.setLocation(target.getX() + 6, target.getY() + (target.getHeight() - hintHeight) / 2);
        }
    }

    /**
     * 提示宽度上限:密钥/确认密钥预留眼睛按钮宽度,算法预留下拉箭头宽度
     * @param anchor
     * @param target
     * @return
     */
    private int hintMaxWidth(GuiAnchor anchor, JComponent target) {
        int reserved = 12;
        if (anchor == GuiAnchor.SECRET_KEY || anchor == GuiAnchor.CONFIRM_KEY) {
            reserved = 52;
        } else if (anchor == GuiAnchor.ALGORITHM) {
            reserved = 24;
        }
        return target.getWidth() - reserved;
    }

    /**
     * 按错误锚点映射面板字段
     * @param anchor
     * @return
     */
    private JComponent anchorComponent(GuiAnchor anchor) {
        switch (anchor) {
            case FILE:
                return dropPanel;
            case SECRET_KEY:
                return secretKeyField;
            case CONFIRM_KEY:
                return confirmKeyField;
            case ALGORITHM:
                return algorithmCombo;
            default:
                return submitButton;
        }
    }

    /**
     * 隐藏错误提示
     */
    public void clearError() {
        fieldHint.dismiss();
    }

    /**
     * 表单全量重置回初始状态:清空文件列表、密钥、确认密钥、目标目录、
     * 选项与错误提示,模式回到Encrypt、算法回到AES
     */
    public void reset() {
        fileListModel.clear();
        secretKeyField.clear();
        confirmKeyField.clear();
        targetPathField.setText("");
        deleteCheckBox.setSelected(false);
        onlyLocalCheckBox.setSelected(false);
        clearError();
        // 模式与算法复位(switchMode联动确认密钥行显隐与布局)
        modeToggle.setSelectedIndex(0);
        algorithmCombo.setSelectedItem("AES");
    }

    /**
     * 预填表单(来自--gui的剩余命令行参数)
     * @param form
     */
    public void prefill(EncryptFormDTO form) {
        if (form.getSourceFilePaths() != null) {
            for (var path : form.getSourceFilePaths()) {
                addFilePath(path);
            }
        }
        if (form.getEncryptAlgorithm() != null && !form.getEncryptAlgorithm().isBlank()) {
            algorithmCombo.setSelectedItem(form.getEncryptAlgorithm());
        }
        if (form.getTargetPath() != null) {
            targetPathField.setText(form.getTargetPath());
        }
        deleteCheckBox.setSelected(form.isDelete());
        onlyLocalCheckBox.setSelected(form.isOnlyLocal());
        switchMode(form.isEncrypt());
    }

    /**
     * 表单提交,组装EncryptFormDTO并回调
     */
    private void submit() {
        clearError();
        var form = new EncryptFormDTO();
        form.setEncrypt(isEncryptMode());
        var paths = new String[fileListModel.size()];
        fileListModel.copyInto(paths);
        form.setSourceFilePaths(new ArrayList<>(Arrays.asList(paths)));
        form.setSecretKey(secretKeyField.getPassword());
        if (form.isEncrypt()) {
            form.setConfirmSecretKey(confirmKeyField.getPassword());
        }
        form.setEncryptAlgorithm((String) algorithmCombo.getSelectedItem());
        form.setDelete(deleteCheckBox.isSelected());
        form.setOnlyLocal(onlyLocalCheckBox.isSelected());
        String targetPath = targetPathField.getText();
        if (targetPath != null && !targetPath.isBlank()) {
            form.setTargetPath(targetPath);
        }
        listener.onSubmit(form);
    }

    /**
     * 当前是否为加密模式
     * @return
     */
    private boolean isEncryptMode() {
        return modeToggle.getSelectedIndex() == 0;
    }
}
