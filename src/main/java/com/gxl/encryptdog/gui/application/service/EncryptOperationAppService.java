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

package com.gxl.encryptdog.gui.application.service;

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.gui.application.dto.EncryptFormDTO;
import com.gxl.encryptdog.gui.application.dto.OperationListener;
import com.gxl.encryptdog.gui.application.dto.OperationResultDTO;
import com.gxl.encryptdog.gui.application.error.GuiAnchor;
import com.gxl.encryptdog.gui.application.error.GuiException;
import com.gxl.encryptdog.gui.infrastructure.acl.EncryptCoreFacade;
import com.gxl.encryptdog.utils.Utils;

import java.util.List;
import java.util.Objects;

/**
 * GUI加/解密操作应用服务,编排校验→解析→确认→执行→回显业务流程。
 * 校验规则与终端语义一致,本层不依赖任何core类型。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/25 18:00
 */
public class EncryptOperationAppService {
    /**
     * 加密核心防腐层
     */
    private final EncryptCoreFacade facade = new EncryptCoreFacade();

    /**
     * 表单参数校验,校验规则与终端ConsoleParamValidator语义一致。
     * 密钥一致性校验仅加密模式执行,对齐终端"解密不二次确认"语义。
     * @param form
     * @throws GuiException
     */
    public void validate(EncryptFormDTO form) throws GuiException {
        // 仅支持macos操作系统
        if (!Utils.isMacOperatingSystem()) {
            throw new GuiException("Currently supporting the Mac operating system", GuiAnchor.SYSTEM);
        }
        // 源文件不能为空
        if (Objects.isNull(form.getSourceFilePaths()) || form.getSourceFilePaths().isEmpty()) {
            throw new GuiException("Source file cannot be empty", GuiAnchor.FILE);
        }
        // 秘钥不能为空
        if (Objects.isNull(form.getSecretKey()) || form.getSecretKey().length < 1) {
            throw new GuiException("Secret-key cannot be empty", GuiAnchor.SECRET_KEY);
        }
        // 秘钥长度校验
        if (form.getSecretKey().length < Constants.DEFAULT_PWD_LENGTH) {
            throw new GuiException("The length of the secret key shall be at least 6 digits", GuiAnchor.SECRET_KEY);
        }
        // 加密模式下校验两次秘钥一致性
        if (form.isEncrypt()) {
            if (Objects.isNull(form.getConfirmSecretKey()) || !Objects.equals(new String(form.getSecretKey()), new String(form.getConfirmSecretKey()))) {
                throw new GuiException("The two secret-key do not match", GuiAnchor.CONFIRM_KEY);
            }
        }
        // 加密算法校验,非法算法在解析前拦截
        if (Objects.isNull(EncryptTypeEnum.check(form.getEncryptAlgorithm()))) {
            throw new GuiException("Non-existent encryption algorithm.", GuiAnchor.ALGORITHM);
        }
    }

    /**
     * 校验并解析源文件列表(目录递归/后缀过滤/容量校验在防腐层内完成),供确认弹窗展示
     * @param form
     * @return
     * @throws GuiException
     */
    public List<String> prepareOperation(EncryptFormDTO form) throws GuiException {
        validate(form);
        // 委托防腐层执行文件解析,解析失败锚定文件拖拽区
        try {
            return facade.parseFiles(form);
        } catch (GuiException e) {
            throw new GuiException(e.getMessage(), e, GuiAnchor.FILE);
        }
    }

    /**
     * 预览各源文件对应的目标文件全限定名
     * @param form
     * @param confirmedFiles
     * @return
     * @throws GuiException
     */
    public List<String> previewTargetFiles(EncryptFormDTO form, List<String> confirmedFiles) throws GuiException {
        return facade.previewTargetFiles(form, confirmedFiles);
    }

    /**
     * 执行加/解密操作,阻塞直至完成,由调用方在工作线程中执行
     * @param form
     * @param confirmedFiles 确认后的源文件列表
     * @param listener 进度回调
     * @return
     * @throws GuiException
     */
    public OperationResultDTO execute(EncryptFormDTO form, List<String> confirmedFiles, OperationListener listener) throws GuiException {
        return facade.execute(form, confirmedFiles, listener);
    }
}
