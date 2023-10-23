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

package com.gxl.encryptdog.core.shell.view;

import com.gxl.encryptdog.base.error.ViewRenderException;
import com.gxl.encryptdog.core.operation.proxy.params.DashboardViewState;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.utils.Utils;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.Style;
import com.taobao.text.ui.*;
import com.taobao.text.util.RenderUtil;

import java.util.*;

/**
 * 视图渲染工具类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/2 11:41
 */
public class ViewRenderUtil {
    /**
     * 交互提示信息渲染
     * @param list
     * @return
     * @throws ViewRenderException
     */
    public static String drawReminderInfo(List<String> list) throws ViewRenderException {
        if (list.isEmpty()) {
            return null;
        }
        try {
            String[] titles = { "NO", "SOURCE_FILE" };
            // 构建通用渲染模版
            var tableElement = buildTableElement(titles);
            // 数据构建
            for (var i = 0; i < list.size(); i++) {
                var row = Element.row();
                row.add(Element.label(String.valueOf(i + 1)));
                row.add(Element.label(Utils.lengthShear(list.get(i), 60)));
                tableElement.add(row);
            }
            return RenderUtil.render(tableElement);
        } catch (Throwable e) {
            throw new ViewRenderException("View rendering failed", e);
        }
    }

    /**
     * 加/解密操作结束时的提示信息
     * @param context
     * @param timeConsuming
     * @param isEncrypt
     * @return
     * @throws ViewRenderException
     */
    public static String drawFinishedInfo(ResultContext context, String timeConsuming, boolean isEncrypt) throws ViewRenderException {
        if (Objects.isNull(context)) {
            return null;
        }
        try {
            String[] titles = { "OPERATION", "S_COUNT", "F_COUNT", "S_RATE", "F_RATE", "TIME_CONSUMING" };
            // 构建通用渲染模版
            var tableElement = buildTableElement(titles);

            // 数据构建
            var row = Element.row();
            row.add(Element.label(Utils.getOperateType(isEncrypt)));
            // 获取成功文件个数
            var successFileNumber = context.getSuccessFileNumber().get();
            row.add(Element.label(String.valueOf(successFileNumber)));
            // 获取失败文件个数
            var failedFileNumber = context.getFailedFileNumber().get();
            row.add(Element.label(String.valueOf(failedFileNumber)));
            var count = successFileNumber + failedFileNumber;
            // 成功率
            row.add(Element.label(String.valueOf(successFileNumber / count * 100) + "%"));
            // 失败率
            row.add(Element.label(String.valueOf(failedFileNumber / count * 100) + "%"));
            // 整体耗时
            row.add(timeConsuming);
            tableElement.add(row);

            return RenderUtil.render(tableElement);
        } catch (Throwable e) {
            throw new ViewRenderException("View rendering failed", e);
        }
    }

    /**
     * 渲染错误提示信息
     * @param rlt
     * @param sourceFile
     * @param msg
     * @return
     * @throws ViewRenderException
     */
    public static String drawErrorInfo(String rlt, String sourceFile, String msg) throws ViewRenderException {
        // 是否显示文件名
        var isDisplayFileName = Objects.nonNull(sourceFile) && !sourceFile.isBlank();
        try {
            String[] titles = null;
            if (isDisplayFileName) {
                titles = new String[] { "RESULT", "FILE", "ERROR_MSG" };
            } else {
                titles = new String[] { "RESULT", "ERROR_MSG" };
            }
            // 构建通用渲染模版
            var tableElement = buildTableElement(titles);
            // 数据构建
            var row = Element.row();
            row.add(Element.label(rlt));
            if (isDisplayFileName) {
                // 超过25个字符就裁剪
                sourceFile = Utils.lengthShear(sourceFile, 25);
                row.add(Element.label(sourceFile));
            }
            row.add(Element.label(msg));
            tableElement.add(row);
            return RenderUtil.render(tableElement);
        } catch (Throwable e) {
            throw new ViewRenderException("View rendering failed", e);
        }
    }

    /**
     * 构建通用渲染模版
     * @param titles
     * @return
     * @throws ViewRenderException
     */
    private static TableElement buildTableElement(String[] titles) throws ViewRenderException {
        try {
            var tableElement = new TableElement().border(BorderStyle.DASHED).separator(BorderStyle.DASHED);
            // 设置单元格的左右边框间隔
            tableElement.leftCellPadding(1).rightCellPadding(1);
            // 设置header
            tableElement.row(true, titles);
            // Overflow.WRAP表示会向外面排出去，即当输出宽度有限时，右边的列可能会显示不出，被挤掉了
            tableElement.overflow(Overflow.HIDDEN);
            return tableElement;
        } catch (Throwable e) {
            throw new ViewRenderException("Template rendering failed", e);
        }
    }

    /**
     * 渲染任务的执行状态
     * @param width
     * @param height
     * @param context
     * @return
     */
    public static String drawTaskStateInfo(int width, int height, ResultContext context) {
        var table = new TableElement(1, 5, 2, 2, 2, 2, 3, 5, 2).overflow(Overflow.HIDDEN).rightCellPadding(1);

        // 添加header信息
        table.add(new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add(
            // 任务编号
            "NO",
            // 源文件全限定名
            "SOURCE_FILE",
            // 源文件大小
            "BEFORE_SIZE",
            // 目标文件大小
            "AFTER_SIZE",
            // 当前任务的执行状态
            "STATE",
            // 当前任务的进度
            "%PROGRESS",
            // 当前任务的预计执行耗时
            "ESTIMATED_TIME",
            // 目标文件全限定名
            "TARGET_FILE",
            // 当前任务的执行结果
            "RESULT"));

        // 任务计数器
        var count = 0;
        List<DashboardViewState> entrys = new ArrayList<>(context.getDashboardViewStates().values());
        Collections.sort(entrys, new ResultComparator());
        for (var entry : entrys) {
            var task = entry;
            // height - 1的目的是因为header要占用1行
            if (++count > height - 1) {
                break;
            }
            // 设置状态label的样式
            var stateLabel = new LabelElement(task.getState());
            switch (task.getState()) {
                case RUNNING:
                    // 如果运行中的stateLabel设置为绿色
                    stateLabel.setStyle(Style.style(Color.green));
                    break;
                case WAITING:
                    // 如果运行中的stateLabel设置为黄色
                    stateLabel.setStyle(Style.style(Color.yellow));
                    break;
                default:
                    // 其它执行状态的stateLabel设置为蓝绿色
                    stateLabel.setStyle(Style.style(Color.cyan));
                    break;
            }

            // 设置结果label的样式
            var rltLabel = new LabelElement(task.getResult().getResult());
            switch (task.getResult()) {
                case SUCCESS:
                    // 如果成功的rltLabel设置为绿色
                    rltLabel.setStyle(Style.style(Color.green));
                    break;
                case FAILED:
                    // 如果失败的rltLabel设置为品红色
                    rltLabel.setStyle(Style.style(Color.magenta));
                    break;
                default:
                    break;
            }

            // 数据填充
            table.row(new LabelElement(count), new LabelElement(Utils.lengthShear(task.getTaskName(), 25)), new LabelElement(task.getSourceFileSize()),
                new LabelElement(task.getTargetFileSize()), stateLabel, new LabelElement(task.getProgress()), new LabelElement(task.getEstimatedTime()),
                new LabelElement(Utils.lengthShear(task.getTargetFile(), 25)), rltLabel);
        }
        return RenderUtil.render(table, width, height);
    }

    /**
     * 渲染执行结果数据
     * @param width
     * @param height
     * @param context
     * @return
     */
    public static String drawTaskResultInfo(int width, int height, ResultContext context) {
        var table = new TableElement(1, 1, 1, 1, 1, 1, 1).overflow(Overflow.HIDDEN).rightCellPadding(1);

        // 添加header信息
        table.add(new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add(
            // 本次操作类型
            "OPERATION",
            // 总文件个数
            "FILE_SIZE",
            // 成功数
            "SUCCESS_COUNT",
            // 失败数
            "FAILED_COUNT",
            // 成功率
            "SUCCESS_RATE",
            // 失败率
            "FAILED_RATE",
            // 执行总耗时
            "TIME_CONSUMING"));

        var viewResult = context.getDashboardViewResult();

        // 设置状态successCountLabel的样式
        var successCountLabel = new LabelElement(viewResult.getSuccessFileNumber().get()).style(Style.style(Color.green));
        // 设置状态successCountLabel的样式
        var failedCountLabel = new LabelElement(viewResult.getFailedFileNumber().get()).style(Style.style(Color.magenta));
        // 设置状态successRateLabel的样式
        var successRateLabel = new LabelElement(viewResult.getSuccessRate()).style(Style.style(Color.green));
        // 设置状态failedRateLabel的样式
        var failedRateLabel = new LabelElement(viewResult.getFailedRate()).style(Style.style(Color.magenta));

        // 数据填充
        table.row(
            // 添加操作类型
            new LabelElement(viewResult.getOperation()),
            // 添加文件总数
            new LabelElement(viewResult.getFileSize()),
            // 添加成功数
            successCountLabel,
            // 添加失败数
            failedCountLabel,
            // 添加成功率
            successRateLabel,
            // 添加失败率
            failedRateLabel,
            // 添加总执行耗时
            new LabelElement(viewResult.getTimeConsuming()));
        return RenderUtil.render(table, width, height);
    }
}
