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

package com.gxl.encryptdog.core.operation;

import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.enums.EncryptStateEnum;
import com.gxl.encryptdog.base.enums.EncryptTypeEnum;
import com.gxl.encryptdog.base.enums.ExecResultEnum;
import com.gxl.encryptdog.core.event.observer.impl.ObServerContextImpl;
import com.gxl.encryptdog.core.operation.impl.encrypt.AesEncrypt;
import com.gxl.encryptdog.core.operation.proxy.params.DashboardViewState;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.core.shell.request.ConsoleRequest;
import com.gxl.encryptdog.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * 打开文件失败路径回归测试(任务1.2)
 * <p>
 * 复现"解析时存在、执行前消失"场景:注册viewState后删除源文件,执行任务断言:
 * 无异常抛出、行到达FINISHED失败终态、失败计数+1、errorMsg为原始错误信息、
 * 目标大小显示-(目标未产生)。
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/9/6 14:30
 */
public class OpenFailureOperationTest {
    @Test
    public void openFailureReachesFailedTerminalState() throws IOException {
        // 准备一个真实文件(带内容,容量校验需要非空),注册viewState后删除,复现执行前消失
        var tempDir = Files.createTempDirectory("encryptdog-openfail").toFile();
        var source = new File(tempDir, "gone.txt");
        Files.write(source.toPath(), "hello dog".getBytes());

        var request = new ConsoleRequest();
        request.setEncrypt(true);
        var vo = new OperationVO();
        vo.setEncrypt(true);
        vo.setDelete(false);
        vo.setEncryptAlgorithm(EncryptTypeEnum.AES);
        vo.setOnlyLocal(false);
        vo.setSourceFilePath(source.getPath());
        // 与防腐层一致的领域模型构建路径(目标文件全限定名)
        vo.setTargetFile(request, source.getPath());
        vo.setSecretKey("123456".toCharArray());
        vo.setSourceFileCapacity(Utils.getFileCapacity(source.getPath()));

        // 注册执行状态(与initDashboard一致),随后源文件消失
        var resultContext = new ResultContext();
        resultContext.getDashboardViewStates().put(source.getPath(), new DashboardViewState(source.getPath()));
        resultContext.getDashboardViewResult().setFileSize("1");
        Files.delete(source.toPath());

        // 执行操作:打开流失败须走失败事件链,不抛NPE
        new AesEncrypt(new ObServerContextImpl()).execute(vo, resultContext);

        // 断言失败终态
        var vs = resultContext.getDashboardViewStates().get(source.getPath());
        assertEquals("行未到达失败终态", EncryptStateEnum.FINISHED, vs.getState());
        assertEquals("结果未标记为失败", ExecResultEnum.FAILED, vs.getResult());
        assertEquals("失败计数未累加", 1, resultContext.getDashboardViewResult().getFailedFileNumber().get());
        assertEquals("成功计数不应累加", 0, resultContext.getDashboardViewResult().getSuccessFileNumber().get());
        assertNotEquals("失败原因未写入", "-", vs.getErrorMsg());
        assertEquals("目标未产生时大小应显示-", "-", vs.getTargetFileSize());
    }
}
