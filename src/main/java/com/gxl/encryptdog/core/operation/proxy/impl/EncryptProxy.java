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

package com.gxl.encryptdog.core.operation.proxy.impl;

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.common.model.OperationContext;
import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.common.tp.impl.CachedThreadPool;
import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.BaseException;
import com.gxl.encryptdog.base.error.OperationException;
import com.gxl.encryptdog.base.error.ValidateException;
import com.gxl.encryptdog.core.event.FinishedEvent;
import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.event.observer.impl.ObServerContextImpl;
import com.gxl.encryptdog.core.operation.OperationStrategy;
import com.gxl.encryptdog.core.operation.proxy.Proxy;
import com.gxl.encryptdog.core.operation.proxy.params.DashboardViewState;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.core.operation.proxy.schedule.ViewSchedule;
import com.gxl.encryptdog.core.shell.view.View;
import com.gxl.encryptdog.core.shell.view.impl.DashboardView;
import com.gxl.encryptdog.utils.Utils;
import com.gxl.encryptdog.utils.player.impl.WavPlayer;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 加/解密执行代理实现
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/3 11:34
 */
@Slf4j
public class EncryptProxy implements Proxy {
    /**
     * 策略集合
     */
    private Map<ChannelEnum, OperationStrategy> channels        = new ConcurrentHashMap<>();
    /**
     * CachedThreadPool工作线程组
     */
    private ExecutorService                     executorService = new CachedThreadPool().buildExecutor();
    /**
     * 执行结果数据上下文
     */
    private ResultContext                       resultContext   = new ResultContext();
    /**
     * Dashboard渲染
     */
    private View                                view            = new DashboardView();
    /**
     * 事件广播器
     */
    private ObServerContext                     obServer        = new ObServerContextImpl();

    public EncryptProxy() {
        // 初始化加/解密器
        initOperationList();
    }

    /**
     * 执行加/解密操作
     *
     * @param context
     */
    @Override
    public void invoke(OperationContext context) throws BaseException {
        // 记录开始时间
        var begin = System.currentTimeMillis();
        try {
            // 获取加/解密操作领域模型
            var voList = context.getOperationVOList();
            // 初始化Dashboard数据
            initDashboard(voList, context.getConsoleRequest().isEncrypt());
            var latch = new CountDownLatch(voList.size() + 1);
            for (var vo : voList) {
                // 执行加/解密操作
                executorService.execute(new EncryptExecuter(getChannel(context), vo, resultContext, latch));
            }

            // 定时渲染dashboard视图
            ViewSchedule.renderDashboardView(view, resultContext, latch);
            latch.await();

            // 下发FinishedEvent事件
            obServer.fireEvent(buildFinishedEvent(begin));

            // 停止视图渲染
            ViewSchedule.stop();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new BaseException(e.getMessage(), e);
        }
    }

    /**
     * 构建FinishedEvent
     *
     * @param begin
     * @return
     */
    private FinishedEvent buildFinishedEvent(long begin) {
        var event = new FinishedEvent(null, System.currentTimeMillis());
        event.setPlayer(new WavPlayer());
        event.setResultContext(resultContext);

        // 计算整体耗时
        var time = Utils.currentTimeFormat((System.currentTimeMillis() - begin) / 1000);
        event.setTimeConsuming(time);
        return event;
    }

    /**
     * Dashboard数据初始化
     * @param voList
     * @param isEncrypt
     * @throws ValidateException
     */
    private void initDashboard(List<OperationVO> voList, boolean isEncrypt) throws ValidateException {
        if (voList.isEmpty()) {
            throw new ValidateException("Input information cannot be null.");
        }
        var viewStates = resultContext.getDashboardViewStates();

        // 从领域模型中获取加密算法类型
        String encryptAlgorithm = voList.get(0).getEncryptAlgorithm().getAlgorithmType();
        for (var operationVO : voList) {
            var key = operationVO.getSourceFilePath();
            var rlt = new DashboardViewState(key);
            // 设置目标文件全限定名
            rlt.setTargetFile(operationVO.getTargetFilePath())
                // 设置源文件大小
                .setSourceFileSize(Utils.capacityFormat(Utils.getFileCapacity(key)));
            viewStates.put(key, rlt);
        }

        var viewResult = resultContext.getDashboardViewResult();
        // 设置本次操作类型
        viewResult.setOperation(Utils.getOperateType(isEncrypt));
        // 设置加密算法类型
        viewResult.setEncryptAlgorithm(encryptAlgorithm);
        // 设置总文件数量
        viewResult.setFileSize(String.valueOf(voList.size()));
    }

    /**
     * 获取渠道
     *
     * @param context
     * @return
     * @throws OperationException
     */
    private OperationStrategy getChannel(OperationContext context) throws OperationException {
        var request = context.getConsoleRequest();
        // 是否是加密
        var isEncrypt = request.isEncrypt();
        // 加密算法类型
        var encryptAlgorithm = request.getEncryptAlgorithm();
        // 获取渠道
        var channel = ChannelEnum.getChannel(isEncrypt, encryptAlgorithm);
        if (Objects.isNull(channel)) {
            throw new OperationException("Non-existent encryption algorithm.");
        }
        // 根据映射关系获取具体的加/解密器
        return channels.get(channel);
    }

    /**
     * 初始化加/解密器
     */
    private void initOperationList() {
        if (!channels.isEmpty()) {
            return;
        }
        try {
            // 获取目标路径下的所有类型
            var reflections = new Reflections(Constants.DEFAULT_SCAN_OPERATION_CLS_PATH);
            // 获取目标类型的所有子类
            for (var cls : reflections.getSubTypesOf(OperationStrategy.class)) {
                // 排除abstract和interface
                if (Modifier.isAbstract(cls.getModifiers()) || Modifier.isInterface(cls.getModifiers())) {
                    continue;
                }
                var channel = cls.getConstructor(ObServerContext.class).newInstance(obServer);
                // 构建映射关系
                channels.put(channel.getChannel(), channel);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            //...
        }
    }
}
