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

package com.gxl.encryptdog.gui.infrastructure.acl;

import com.gxl.encryptdog.base.common.model.OperationContext;
import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.error.BaseException;
import com.gxl.encryptdog.core.operation.proxy.impl.EncryptProxy;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.core.parse.Parser;
import com.gxl.encryptdog.core.parse.impl.FileNameParser;
import com.gxl.encryptdog.core.shell.request.ConsoleRequest;
import com.gxl.encryptdog.gui.application.dto.EncryptFormDTO;
import com.gxl.encryptdog.gui.application.dto.OperationListener;
import com.gxl.encryptdog.gui.application.dto.OperationProgressDTO;
import com.gxl.encryptdog.gui.application.dto.OperationResultDTO;
import com.gxl.encryptdog.gui.application.error.GuiException;
import com.gxl.encryptdog.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 加密核心防腐层,GUI上下文中唯一接触core限界上下文的地方。
 * 负责表单模型与core模型的双向转换、执行加/解密操作,core类型不向应用层与表现层泄漏。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class EncryptCoreFacade {
    /**
     * 文件解析器
     */
    private final Parser fileParser = new FileNameParser();

    /**
     * 解析文件列表(含目录递归/后缀过滤/容量校验),语义与终端FileNameParser一致
     * @param form
     * @return
     * @throws GuiException
     */
    public List<String> parseFiles(EncryptFormDTO form) throws GuiException {
        var request = buildConsoleRequest(form, form.getSourceFilePaths());
        try {
            // 执行文件解析
            return fileParser.parse(request);
        } catch (BaseException e) {
            throw new GuiException(e.getMessage(), e);
        }
    }

    /**
     * 预览各源文件对应的目标文件全限定名,用于操作确认弹窗展示
     * @param form
     * @param files 解析后的源文件列表
     * @return
     * @throws GuiException
     */
    public List<String> previewTargetFiles(EncryptFormDTO form, List<String> files) throws GuiException {
        var request = buildConsoleRequest(form, files);
        var rlt = new ArrayList<String>();
        for (var operationVO : buildOperationVOList(request)) {
            rlt.add(operationVO.getTargetFilePath());
        }
        return rlt;
    }

    /**
     * 执行加/解密操作,阻塞直至全部任务完成。
     * 每次操作新建执行代理以保证结果上下文状态隔离,并注入GUI视图以刷新进度。
     * @param form
     * @param confirmedFiles 确认后的源文件列表
     * @param listener 进度回调
     * @return
     * @throws GuiException
     */
    public OperationResultDTO execute(EncryptFormDTO form, List<String> confirmedFiles, OperationListener listener) throws GuiException {
        var request = buildConsoleRequest(form, confirmedFiles);
        var context = new OperationContext().setOperationVOList(buildOperationVOList(request)).setConsoleRequest(request);

        // 每次操作新建执行代理,保证ResultContext等状态不跨操作残留
        var proxy = new EncryptProxy();
        var view = new GuiDashboardView(listener, this);
        // 注入GUI视图,由ViewSchedule定时回调刷新进度
        proxy.setView(view);
        try {
            // 执行加/解密操作
            proxy.invoke(context);
        } catch (BaseException e) {
            throw new GuiException(e.getMessage(), e);
        }
        // 将执行结果上下文转换为GUI结果模型
        return convertResult(view.getResultContext());
    }

    /**
     * 执行结果上下文转进度快照
     * @param context
     * @return
     */
    OperationProgressDTO convertProgress(ResultContext context) {
        var dto = new OperationProgressDTO();
        var viewResult = context.getDashboardViewResult();
        dto.setOperation(viewResult.getOperation());
        dto.setEncryptAlgorithm(viewResult.getEncryptAlgorithm());
        dto.setTotalFiles(context.getDashboardViewStates().size());

        var fileProgressList = dto.getFileProgressList();
        for (var entry : context.getDashboardViewStates().entrySet()) {
            var vs = entry.getValue();
            var fp = new OperationProgressDTO.FileProgress();
            fp.setSourceFile(vs.getTaskName());
            fp.setProgress(vs.getProgress());
            fp.setEstimatedTime(vs.getEstimatedTime());
            fp.setSourceFileSize(vs.getSourceFileSize());
            fp.setTargetFile(vs.getTargetFile());
            fp.setTargetFileSize(Objects.isNull(vs.getTargetFileSize()) ? "-" : vs.getTargetFileSize());
            fp.setResult(vs.getResult().getResult());
            // 状态取核心枚举定义的值(WAITING/RUNNING/FINISHED),GUI直接展示
            fp.setState(vs.getState().getState());
            fileProgressList.add(fp);
        }
        return dto;
    }

    /**
     * 执行结果上下文转结果模型
     * @param context
     * @return
     */
    private OperationResultDTO convertResult(ResultContext context) {
        var dto = new OperationResultDTO();
        var viewResult = context.getDashboardViewResult();
        dto.setOperation(viewResult.getOperation());
        dto.setEncryptAlgorithm(viewResult.getEncryptAlgorithm());
        dto.setTotalFiles(context.getDashboardViewStates().size());
        dto.setSuccessCount(viewResult.getSuccessFileNumber().get());
        dto.setFailedCount(viewResult.getFailedFileNumber().get());
        dto.setSuccessRate(viewResult.getSuccessRate());
        dto.setFailedRate(viewResult.getFailedRate());
        dto.setTimeConsuming(viewResult.getTimeConsuming());

        var fileResults = dto.getFileResults();
        for (var vs : context.getDashboardViewStates().values()) {
            var fr = new OperationResultDTO.FileResult();
            fr.setSourceFile(vs.getTaskName());
            fr.setTargetFile(vs.getTargetFile());
            fr.setResult(vs.getResult().getResult());
            fr.setErrorMsg(vs.getErrorMsg());
            fr.setSourceFileSize(vs.getSourceFileSize());
            fr.setTargetFileSize(vs.getTargetFileSize());
            fileResults.add(fr);
        }
        return dto;
    }

    /**
     * 表单模型转core请求参数
     * @param form
     * @param files
     * @return
     */
    private ConsoleRequest buildConsoleRequest(EncryptFormDTO form, List<String> files) {
        var request = new ConsoleRequest();
        request.setSourceFiles(files);
        request.setTargetPath(form.getTargetPath());
        request.setSecretKey(form.getSecretKey());
        request.setEncrypt(form.isEncrypt());
        request.setDelete(form.isDelete());
        request.setOnlyLocal(form.isOnlyLocal());
        request.setEncryptAlgorithm(form.getEncryptAlgorithm());
        return request;
    }

    /**
     * 构建OperationVO列表,语义与终端EncryptDogConsole#buildOperationVO一致
     * @param request
     * @return
     * @throws GuiException
     */
    private List<OperationVO> buildOperationVOList(ConsoleRequest request) throws GuiException {
        var rlt = new ArrayList<OperationVO>();
        // 遍历源文件
        for (var sf : request.getSourceFiles()) {
            var operationVO = new OperationVO();
            operationVO.setEncrypt(request.isEncrypt());
            operationVO.setDelete(request.isDelete());
            // 校验并设置加密算法类型
            var encryptType = EncryptTypeEnum.check(request.getEncryptAlgorithm());
            if (Objects.isNull(encryptType)) {
                throw new GuiException("Non-existent encryption algorithm.");
            }
            operationVO.setEncryptAlgorithm(encryptType);
            operationVO.setOnlyLocal(request.isOnlyLocal());
            operationVO.setSourceFilePath(sf);
            operationVO.setTargetFile(request, sf);
            operationVO.setSecretKey(request.getSecretKey());
            operationVO.setSourceFileCapacity(Utils.getFileCapacity(sf));
            rlt.add(operationVO);
        }
        return rlt;
    }
}
