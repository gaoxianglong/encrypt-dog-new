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

package com.gxl.encryptdog.core.shell;

import com.gxl.encryptdog.base.common.model.OperationContext;
import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.error.*;
import com.gxl.encryptdog.core.operation.proxy.Proxy;
import com.gxl.encryptdog.core.operation.proxy.impl.EncryptProxy;
import com.gxl.encryptdog.core.parse.Parser;
import com.gxl.encryptdog.core.parse.impl.FileNameParser;
import com.gxl.encryptdog.core.shell.request.ConsoleParamValidator;
import com.gxl.encryptdog.core.shell.request.ConsoleRequest;
import com.gxl.encryptdog.utils.ConsoleWriter;
import com.gxl.encryptdog.utils.Utils;
import lombok.Data;
import picocli.CommandLine;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * EncryptDog控制台程序
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/9/21 23:24
 */
@Data
@CommandLine.Command(name = "encrypt-dog", footer = "Copyright(c) 2021 - 2031 gaoxianglong. All Rights Reserved.", mixinStandardHelpOptions = true)
public class EncryptDogConsole extends ConsoleRequest implements Runnable {
    @Serial
    private static final long   serialVersionUID = 5099613718438642688L;

    /**
     * 文件解析器
     */
    private Parser              fileParser       = new FileNameParser();
    /**
     * 加解密执行代理
     */
    private Proxy               proxy            = new EncryptProxy();
    /**
     * 用户确认结果
     */
    private static final String CONFIRM_RESULT   = "Y";

    public EncryptDogConsole() {
        try {
            // 加载Banner信息
            BannerInfo.drawBanner();
        } catch (BaseException e) {
            Tooltips.writeErrorMsg(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // 前置校验操作
            validate();

            // 执行文件解析
            setSourceFiles(fileParser.parse(getConsoleRequest()));

            // 相关用户确认交互
            confirmation();

            // 构建OperationContext
            var context = buildOperationContext(buildOperationVO());

            // 执行加/解密操作
            proxy.invoke(context);
        } catch (ExitException e) {
            // 用户主动退出
            return;
        } catch (BaseException e) {
            Tooltips.writeErrorMsg(e.getSourceFile(), e.getMessage());
        }
    }

    /**
     * 构建OperationContext
     * @param operationVOList
     * @return
     */
    private OperationContext buildOperationContext(List<OperationVO> operationVOList) {
        return new OperationContext().setOperationVOList(operationVOList).setConsoleRequest(getConsoleRequest());
    }

    /**
     * 构建OperationVO
     * @return
     */
    private List<OperationVO> buildOperationVO() {
        var rlt = new ArrayList<OperationVO>();

        // 遍历源文件
        for (var sf : getSourceFiles()) {
            var operationVO = new OperationVO();
            operationVO.setEncrypt(isEncrypt());
            operationVO.setDelete(isDelete());
            // 设置加密算法类型
            setEncryptType(operationVO);
            operationVO.setOnlyLocal(isOnlyLocal());
            operationVO.setSourceFilePath(sf);
            operationVO.setTargetFile(getConsoleRequest(), sf);
            operationVO.setSecretKey(getSecretKey());
            operationVO.setSourceFileCapacity(Utils.getFileCapacity(sf));
            rlt.add(operationVO);
        }
        return rlt;
    }

    /**
     * 设置加密算法类型
     * @param operationVO
     */
    private void setEncryptType(OperationVO operationVO) {
        // 获取加密算法类型
        var encryptType = EncryptTypeEnum.check(getEncryptAlgorithm());
        operationVO.setEncryptAlgorithm(encryptType);
    }

    /**
     * 操作确认交互
     * @throws BaseException
     */
    private void confirmation() throws BaseException {
        // 源文件确认交互
        sourceFileConfirmation();

        // 删除源文件确认交互
        deleteSourceFileConfirmation();
    }

    /**
     * 源文件确认交互
     * @throws ExitException
     * @throws ViewRenderException
     */
    private void sourceFileConfirmation() throws ExitException, ViewRenderException {
        var sfs = getSourceFiles();
        if (Objects.isNull(sfs) || sfs.isEmpty()) {
            return;
        }
        // 控制台输出源文件列表信息
        Tooltips.writeSourceFileList(sfs);
        if (!CONFIRM_RESULT.equalsIgnoreCase(Utils.consoleInput())) {
            ConsoleWriter.write("Bye~", true);
            // 用户主动退出
            throw new ExitException("User actively exits");
        }
    }

    /**
     * 删除源文件确认交互
     */
    private void deleteSourceFileConfirmation() {
        if (!isDelete()) {
            return;
        }
        // 控制台输出删除源文件提示信息
        Tooltips.writeDeleteSourceFile();
        if (!CONFIRM_RESULT.equalsIgnoreCase(Utils.consoleInput())) {
            setDelete(false);
        }
    }

    /**
     * 前置校验操作
     * @throws ValidateException
     */
    private void validate() throws ValidateException {
        // 仅支持macos操作系统
//        if (!Utils.isMacOperatingSystem()) {
//            throw new ValidateException("Currently supporting the Mac operating system");
//        }

        // 参数校验
        ConsoleParamValidator.validate(getConsoleRequest());
    }
}
