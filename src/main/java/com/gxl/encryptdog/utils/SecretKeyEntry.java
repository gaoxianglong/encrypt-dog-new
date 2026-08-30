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

package com.gxl.encryptdog.utils;

import com.gxl.encryptdog.base.error.OperationException;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密钥存储文件条目v2编解码工具
 * <p>
 * 条目格式:v2:&lt;iter&gt;:base64(salt16):base64(nonce12):base64(ct):base64(tag16)
 * 派生:PBKDF2WithHmacSHA256(源秘钥, salt, iter) =&gt; 256bit AES密钥
 * 加密:AES-256-GCM,nonce 12字节随机,tag 128bit,encrypt-then-auth
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2026/8/30 19:00
 */
public class SecretKeyEntry {
    /**
     * 条目格式前缀
     */
    public static final String FORMAT_PREFIX     = "v2";
    /**
     * 缺省PBKDF2迭代次数
     */
    public static final int    DEFAULT_ITERATIONS = 600_000;
    /**
     * 盐长度16bytes
     */
    private static final int   SALT_LENGTH       = 16;
    /**
     * GCM nonce长度12bytes
     */
    private static final int   NONCE_LENGTH      = 12;
    /**
     * GCM tag长度128bit
     */
    private static final int   TAG_BITS          = 128;
    /**
     * 派生密钥长度256bit
     */
    private static final int   KEY_BITS          = 256;
    /**
     * 基于PBKDF2算法使用密码派生函数
     */
    private static final String KEY_DERIVATION    = "PBKDF2WithHmacSHA256";
    /**
     * 条目加密算法
     */
    private static final String CIPHER_ALGORITHM  = "AES/GCM/NoPadding";

    private SecretKeyEntry() {
    }

    /**
     * 加密密钥条目,输出v2自描述格式
     *
     * @param userPassword 用户源秘钥
     * @param secretKey    真实秘钥
     * @return
     * @throws OperationException
     */
    public static String encryptEntry(char[] userPassword, String secretKey) throws OperationException {
        try {
            var salt = new byte[SALT_LENGTH];
            var nonce = new byte[NONCE_LENGTH];
            var random = new SecureRandom();
            random.nextBytes(salt);
            random.nextBytes(nonce);

            var key = deriveKey(userPassword, salt, DEFAULT_ITERATIONS);
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            // GCM加密输出=密文||tag
            var encrypted = cipher.doFinal(secretKey.getBytes(StandardCharsets.UTF_8));

            var tagLength = TAG_BITS / 8;
            var ct = new byte[encrypted.length - tagLength];
            var tag = new byte[tagLength];
            System.arraycopy(encrypted, 0, ct, 0, ct.length);
            System.arraycopy(encrypted, ct.length, tag, 0, tag.length);

            var encoder = Base64.getEncoder();
            return String.join(":", FORMAT_PREFIX, String.valueOf(DEFAULT_ITERATIONS), encoder.encodeToString(salt),
                encoder.encodeToString(nonce), encoder.encodeToString(ct), encoder.encodeToString(tag));
        } catch (Throwable e) {
            throw new OperationException("Secret key entry encryption failed", e);
        }
    }

    /**
     * 解密密钥条目,先校验GCM tag再还原真实秘钥
     *
     * @param userPassword 用户源秘钥
     * @param entry        密钥条目
     * @return
     * @throws OperationException
     */
    public static String decryptEntry(char[] userPassword, String entry) throws OperationException {
        try {
            // 条目自描述格式解析
            var parts = entry.split(":");
            if (6 != parts.length || !FORMAT_PREFIX.equals(parts[0])) {
                throw new OperationException("The secret key record is corrupted, please re-encrypt the file with the original password.");
            }
            // 迭代次数随条目存储
            var iterations = Integer.parseInt(parts[1]);

            var decoder = Base64.getDecoder();
            var salt = decoder.decode(parts[2]);
            var nonce = decoder.decode(parts[3]);
            var ct = decoder.decode(parts[4]);
            var tag = decoder.decode(parts[5]);
            if (SALT_LENGTH != salt.length || NONCE_LENGTH != nonce.length || TAG_BITS / 8 != tag.length) {
                throw new OperationException("The secret key record is corrupted, please re-encrypt the file with the original password.");
            }

            var key = deriveKey(userPassword, salt, iterations);
            var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            // 合并密文与tag后解密
            var payload = new byte[ct.length + tag.length];
            System.arraycopy(ct, 0, payload, 0, ct.length);
            System.arraycopy(tag, 0, payload, ct.length, tag.length);
            return new String(cipher.doFinal(payload), StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            // tag校验失败:密码错误或条目内容被篡改
            throw new OperationException("The password is incorrect, or the secret key record has been tampered with.", e);
        } catch (OperationException e) {
            throw e;
        } catch (Throwable e) {
            throw new OperationException("The secret key record is corrupted, please re-encrypt the file with the original password.", e);
        }
    }

    /**
     * PBKDF2密钥派生
     *
     * @param userPassword
     * @param salt
     * @param iterations
     * @return
     * @throws Exception
     */
    private static SecretKeySpec deriveKey(char[] userPassword, byte[] salt, int iterations) throws Exception {
        var factory = SecretKeyFactory.getInstance(KEY_DERIVATION);
        var spec = new PBEKeySpec(userPassword, salt, iterations, KEY_BITS);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }
}
