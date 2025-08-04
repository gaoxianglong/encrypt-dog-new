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
    int    MAGIC_NUMBER        = 0xDE0225CF;

    /**
     * 魔术长度4bytes
     */
    int    MAGIC_NUMBER_BYTES  = 4;

    /**
     * 文件头长度4bytes
     */
    int    HEADER_LENGTH_BYTES = 4;

    /**
     * 随机盐值长度为16bytes
     */
    int    SALT_LENGTH_BYTES   = 16;

    /**
     * 最高安全性密码存储文件地址
     */
    String SECRET_KEY_FILE     = String.format("%s%s.dog%sDOG-SECRET-KEY.properties", System.getProperty("user.home"), Constants.FILE_SEPARATOR, Constants.FILE_SEPARATOR);

    /**
     * 抽象模版方法
     * @param operationVO
     * @param context
     * @return
     */
    void execute(OperationVO operationVO, ResultContext context);

    /**
     * 解析加密算法跟文件头的加密算法是否一致
     * 加密操作:文件头中指定其加密类型;
     * 解密操作:验证加密算法跟文件头的加密算法是否一致
     *
     * @param context
     * @throws HeaderParseException
     */
    void parseEncryptType(EncryptContext context) throws HeaderParseException;

    /**
     * 解析魔术
     *
     * @param encryptContext
     * @throws MagicNumberParseException
     */
    void parseMagicNumber(EncryptContext encryptContext) throws MagicNumberParseException;

    /**
     * 解析文件头
     * 读操作：解析文件头中的加密算法、IV向量、Salt、Version等信息进行验证
     * 写操作：将加密算法类型、IV向量、Salt等信息记录到FileHeader中
     * 
     * @param context
     * @throws HeaderParseException
     * @throws OperationException
     */
    void parseHeader(EncryptContext context) throws HeaderParseException, OperationException;

    /**
     * 获取分段操作容量
     * 读操作：加密时缺省每次写入10MB
     * 写操作：不同的加密算法分段大小不同
     *
     * @param capacity
     * @return
     */
    int getDefaultCapacity(long capacity);

    /**
     * 是否仅限在相同的物理设备上完成加/解密操作
     *
     * @param encryptContext
     * @throws OperationException
     */
    void bind(EncryptContext encryptContext) throws OperationException;

    /**
     * 解析IV向量操作
     * 读操作：解密时从文件头中解析IV向量值
     * 写操作：加密时生成随机IV向量，并记录到FileHeader中
     *
     * @param encryptContext
     * @throws HeaderParseException
     * @throws OperationException
     */
    void parseVector(EncryptContext encryptContext) throws HeaderParseException, OperationException;

    /**
     * 解析盐值
     * 读操作：解密时从文件头中解析盐值
     * 写操作：加密时生成16bytes随机盐值，并记录到FileHeader中
     * 
     * @param encryptContext
     * @throws HeaderParseException
     */
    void parseSalt(EncryptContext encryptContext) throws HeaderParseException;

    /**
     * 获取渠道
     * @return
     */
    ChannelEnum getChannel();

    /**
     * 加密时向文件头写入头信息
     * 
     * @param encryptContext
     * @throws HeaderParseException
     */
    void saveFileHeader(EncryptContext encryptContext) throws HeaderParseException;

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
     * 这个操作不建议使用，风险非常大，如果绑定的目标设备故障，那么解密可能性为0
     * @throws IOException
     */
    default void createSecretkeyFile() throws IOException {
        Utils.createTargetFile(SECRET_KEY_FILE);
    }
}