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

import com.gxl.encryptdog.base.common.Constants;
import com.gxl.encryptdog.base.common.model.OperationVO;
import com.gxl.encryptdog.base.enums.ChannelEnum;
import com.gxl.encryptdog.base.error.*;
import com.gxl.encryptdog.core.operation.proxy.params.ResultContext;
import com.gxl.encryptdog.utils.Utils;

import java.io.IOException;

/**
 * 加/解密操作策略类
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2023/10/1 10:10
 */
public interface OperationStrategy {
    /**
     * magic number
     */
    int    MAGIC_NUMBER       = 0xDE0225CF;
    /**
     * 魔术长度4bytes
     */
    int    MAGIC_NUMBER_BYTES = 4;
    /**
     * 最高安全性密码存储文件地址
     */
    String SECRET_KEY_FILE    = String.format("%s%s.dog%sDOG-SECRET-KEY.properties", System.getProperty("user.home"), Constants.FILE_SEPARATOR, Constants.FILE_SEPARATOR);

    /**
     * 抽象模版方法
     * @param operationVO
     * @param context
     * @return
     */
    void execute(OperationVO operationVO, ResultContext context);

    /**
     * 加密类型检查
     * @param encryptContext
     * @throws ValidateException
     */
    void checkEncryptType(EncryptContext encryptContext) throws ValidateException, EncryptAlgorithmException;

    /**
     * 魔术检查
     *
     * @param encryptContext
     * @throws MagicNumberException
     */
    void checkMagicNumber(EncryptContext encryptContext) throws MagicNumberException;

    /**
     * 获取分段操作容量
     *
     * @param capacity
     * @return
     */
    int getDefaultCapacity(long capacity);

    /**
     * 是否仅限在相同的物理设备上完成加/解密操作
     *
     * @param encryptContext
     * @throws EncryptException
     */
    void bind(EncryptContext encryptContext) throws EncryptException;

    /**
     * 初始化向量IV操作
     * @param encryptContext
     * @throws OperationException
     */
    void initVector(EncryptContext encryptContext) throws OperationException;

    /**
     * 获取渠道
     * @return
     */
    ChannelEnum getChannel();

    /**
     * 转储加/解密数据
     * @param encryptContext
     * @throws EncryptException
     */
    void store(EncryptContext encryptContext) throws EncryptException;

    /**
     * 成功后处理
     * @param encryptContext
     * @throws OperationException
     */
    void onSuccess(EncryptContext encryptContext) throws OperationException;

    /**
     * 失败后处理
     * @param encryptContext
     * @throws OperationException
     */
    void onFailure(EncryptContext encryptContext) throws OperationException;

    /**
     * 最高安全性-创建秘钥本地固化文件
     * @throws IOException
     */
    default void createSecretkeyFile() throws IOException {
        Utils.createTargetFile(SECRET_KEY_FILE);
    }
}
