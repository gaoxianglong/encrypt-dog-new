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

import com.formdev.flatlaf.FlatDarkLaf;
import com.gxl.encryptdog.gui.application.dto.OperationProgressDTO;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * 执行页滚动位置保持回归测试(任务1.2)
 * <p>
 * 回归两个缺陷:一、进度快照刷新重建列表后JViewport视口位置被重置为顶部
 * (滚动条瞄定位置与视口脱钩,视图被拉回正在加密的文件);二、修复后新操作
 * 不得继承上一轮操作的滚动位置(begin路径显式置顶)。
 * <p>
 * 验证路径:200个文件挂真实窗口→滑到底部→多次refresh刷新→断言视口位置
 * 与滚动条value均保持→再次begin(新一轮操作)→断言从顶部开始。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/6 14:00
 */
public class ProgressPanelScrollTest {
    /**
     * 测试文件数量(超过可视区域约10行,必然出现滚动条)
     */
    private static final int   FILE_COUNT = 200;
    /**
     * 面板挂载尺寸(与执行页宽屏形态同量级)
     */
    private static final int   PANEL_W    = 900;
    /**
     * 面板挂载尺寸(与执行页宽屏形态同量级)
     */
    private static final int   PANEL_H    = 700;

    @BeforeClass
    public static void initLaf() {
        // ProgressPanel依赖UIManager.getFont("defaultFont"),由FlatLaf主题提供
        FlatDarkLaf.setup();
    }

    @Test
    public void refreshKeepsScrollPositionAndBeginResetsTop() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setLayout(null);
                frame.setSize(PANEL_W, PANEL_H);
                ProgressPanel panel = new ProgressPanel(e -> { });
                frame.add(panel);
                frame.setVisible(true);
                // 展示后重复setBounds触发layoutContent(与EncryptDogFrame挂载路径一致),布局滚动容器
                panel.setBounds(0, 0, PANEL_W, PANEL_H);
                frame.validate();

                List<String> files = new ArrayList<>();
                for (int i = 0; i < FILE_COUNT; i++) {
                    files.add("/tmp/fake/source-" + i + ".txt");
                }

                // 进入执行页(全部Waiting)
                panel.begin("ENCRYPT", "AES", files);
                JScrollPane scrollPane = findScrollPane(panel);
                assertNotNull(scrollPane);
                // 测试环境无持续绘制,显式validate同步视口尺寸到滚动条模型(真实app中由绘制循环自然同步)
                scrollPane.validate();

                // 滑到列表底部
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
                Point saved = scrollPane.getViewport().getViewPosition();
                // 前提:200行必须真实产生滚动,否则本测试退化为空洞断言
                assertTrue("测试前提不成立:200行未产生滚动", saved.y > 0);

                // 每秒快照刷新:视口位置与滚动条value必须保持
                panel.refresh(buildSnapshot(files, 40));
                assertEquals("刷新后视口位置被重置到顶部", saved, scrollPane.getViewport().getViewPosition());
                assertEquals("刷新后滚动条与视口脱钩", saved.y, bar.getValue());

                // 连续多次刷新(完成数递增,行持续重排序),位置仍保持
                for (int finished = 50; finished <= 70; finished += 10) {
                    panel.refresh(buildSnapshot(files, finished));
                    assertEquals("第" + finished + "行完成后刷新视口位置被重置", saved,
                            scrollPane.getViewport().getViewPosition());
                    assertEquals(saved.y, bar.getValue());
                }

                // 新一轮操作:显式置顶,不继承上一轮操作的滚动位置
                panel.begin("DECRYPT", "AES", files);
                assertEquals("新操作未从列表顶部开始", new Point(0, 0), scrollPane.getViewport().getViewPosition());

                frame.dispose();
            }
        });
    }

    /**
     * 构建进度快照:前finishedCount个文件已成功完成(FINISHED),随后一个执行中(RUNNING),其余等待(WAITING)
     * @param files 源文件列表
     * @param finishedCount 已完成数量
     * @return 快照
     */
    private static OperationProgressDTO buildSnapshot(List<String> files, int finishedCount) {
        var dto = new OperationProgressDTO();
        dto.setOperation("ENCRYPT");
        dto.setEncryptAlgorithm("AES");
        dto.setTotalFiles(files.size());
        for (int i = 0; i < files.size(); i++) {
            var fp = new OperationProgressDTO.FileProgress();
            fp.setSourceFile(files.get(i));
            fp.setProgress(i < finishedCount ? "100%" : "-");
            fp.setEstimatedTime("-");
            fp.setSourceFileSize("-");
            fp.setTargetFile(files.get(i) + ".enc");
            fp.setTargetFileSize("-");
            fp.setResult(i < finishedCount ? "SUCCESS" : "-");
            fp.setState(i < finishedCount ? "FINISHED" : (i == finishedCount ? "RUNNING" : "WAITING"));
            dto.getFileProgressList().add(fp);
        }
        return dto;
    }

    /**
     * 从面板子树中定位列表滚动容器(ProgressPanel不暴露scrollPane,测试从组件树查找)
     * @param root 根容器
     * @return 找到的JScrollPane,未找到返回null
     */
    private static JScrollPane findScrollPane(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JScrollPane) {
                return (JScrollPane) c;
            }
            if (c instanceof Container) {
                JScrollPane found = findScrollPane((Container) c);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
