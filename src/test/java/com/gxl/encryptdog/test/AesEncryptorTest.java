package com.gxl.encryptdog.test;

import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.impl.decrypt.AesDecryptor;
import com.gxl.encryptdog.core.operation.impl.encrypt.AesEncryptor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * AesEncryptTest
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2025/7/30 17:23
 */
public class AesEncryptorTest {
    private static AesEncryptor encryptor;
    private static AesDecryptor decryptor;

    @BeforeClass
    public static void init() throws Throwable {
        encryptor = AesEncryptor.class.getConstructor(ObServerContext.class).newInstance((ObServerContext) null);
        decryptor = AesDecryptor.class.getConstructor(ObServerContext.class).newInstance((ObServerContext) null);
    }

    @Test
    public void testEncrypt() throws Throwable {
        String source = UUID.randomUUID().toString();
        byte[] salt = initSalt();
        byte[] iv = initVector();

        // 数据加密
        Method dataEncrypt = encryptor.getClass().getDeclaredMethod("dataEncrypt", byte[].class, char[].class, byte[].class, byte[].class);
        dataEncrypt.setAccessible(true);
        byte[] target = (byte[]) dataEncrypt.invoke(encryptor, source.getBytes("utf-8"), "123456".toCharArray(), iv, salt);

        // 数据解密
        Method dataDecrypt = decryptor.getClass().getDeclaredMethod("dataDecrypt", byte[].class, char[].class, byte[].class, byte[].class);
        dataDecrypt.setAccessible(true);
        Assert.assertEquals(source, new String((byte[]) dataDecrypt.invoke(decryptor, target, "123456".toCharArray(), iv, salt)));
    }

    /**
     * 测试加密后数据块大小
     * @throws Throwable
     */
    @Test
    public void testEncryptedSize() throws Throwable {
        byte[] source = new byte[1024 * 1024 * 10];
        byte[] salt = initSalt();
        byte[] iv = initVector();

        // 数据加密
        Method dataEncrypt = encryptor.getClass().getDeclaredMethod("dataEncrypt", byte[].class, char[].class, byte[].class, byte[].class);
        dataEncrypt.setAccessible(true);
        byte[] target = (byte[]) dataEncrypt.invoke(encryptor, source, "123456".toCharArray(), iv, salt);
        // 加密后数据块大小 10485776bytes
        Assert.assertEquals(0xa00010, target.length);
    }

    private byte[] initSalt() {
        // 定长16bytes的盐值
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private byte[] initVector() {
        // 定长12bytes的向量
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
