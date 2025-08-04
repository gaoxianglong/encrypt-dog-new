package com.gxl.encryptdog.test;

import com.gxl.encryptdog.core.event.observer.ObServerContext;
import com.gxl.encryptdog.core.operation.impl.encrypt.ChaCha20Poly1305Encryptor;
import com.gxl.encryptdog.core.operation.impl.decrypt.ChaCha20Poly1305Decryptor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * ChaCha20Poly1305EncryptorTest
 *
 * @author gxl
 * @version Id: 1.0.0
 * @since 2025/7/30 16:24
 */
public class ChaCha20Poly1305EncryptorTest {
    private static ChaCha20Poly1305Encryptor encryptor;
    private static ChaCha20Poly1305Decryptor decryptor;

    @BeforeClass
    public static void init() throws Throwable {
        encryptor = ChaCha20Poly1305Encryptor.class.getConstructor(ObServerContext.class).newInstance((ObServerContext) null);
        decryptor = ChaCha20Poly1305Decryptor.class.getConstructor(ObServerContext.class).newInstance((ObServerContext) null);
    }

    /**
     * 加解密测试
     * @throws Throwable
     */
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

    /**
     * chacha20的向量长度只能是12bytes
     * @return
     */
    private byte[] initVector() {
        // 定长12bytes的向量
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
