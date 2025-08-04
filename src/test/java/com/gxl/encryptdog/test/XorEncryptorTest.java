package com.gxl.encryptdog.test;

import com.gxl.encryptdog.core.operation.impl.decrypt.XorDecryptor;
import com.gxl.encryptdog.core.operation.impl.encrypt.XorEncryptor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * XorEncryptorTest
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2025/7/31 09:57
 */
public class XorEncryptorTest {
    private static XorEncryptor encryptor;
    private static XorDecryptor decryptor;

    @BeforeClass
    public static void init() {
        encryptor = new XorEncryptor(null);
        decryptor = new XorDecryptor(null);
    }

    @Test
    public void testEncrypt() throws Throwable {
        String source = UUID.randomUUID().toString();

        // 数据加密
        Method dataEncrypt = encryptor.getClass().getDeclaredMethod("dataEncrypt", byte[].class, char[].class, byte[].class, byte[].class);
        dataEncrypt.setAccessible(true);
        byte[] target = (byte[]) dataEncrypt.invoke(encryptor, source.getBytes("utf-8"), "123456".toCharArray(), null, null);

        // 数据解密
        Method dataDecrypt = decryptor.getClass().getDeclaredMethod("dataDecrypt", byte[].class, char[].class, byte[].class, byte[].class);
        dataDecrypt.setAccessible(true);
        Assert.assertEquals(source, new String((byte[]) dataDecrypt.invoke(decryptor, target, "123456".toCharArray(), null, null)));
    }

    /**
     * 测试加密后数据块大小
     * @throws Throwable
     */
    @Test
    public void testEncryptedSize() throws Throwable {
        byte[] source = new byte[1024 * 1024 * 10];

        // 数据加密
        Method dataEncrypt = encryptor.getClass().getDeclaredMethod("dataEncrypt", byte[].class, char[].class, byte[].class, byte[].class);
        dataEncrypt.setAccessible(true);
        byte[] target = (byte[]) dataEncrypt.invoke(encryptor, source, "123456".toCharArray(), null, null);
        // 加密后数据块大小 10485760bytes
        Assert.assertEquals(0xa00000, target.length);
    }
}
