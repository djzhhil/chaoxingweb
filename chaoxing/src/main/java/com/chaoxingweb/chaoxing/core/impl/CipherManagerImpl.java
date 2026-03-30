package com.chaoxingweb.chaoxing.core.impl;

import com.chaoxingweb.chaoxing.core.CipherManager;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密管理器实现（AES-CBC）
 *
 * 严格对照 Python 实现：
 * - 使用 AES-CBC 模式
 * - key 和 iv 都是 "u2oh6Vu^HWe4_AES"
 * - 使用 PKCS5 填充（Java 默认）
 * - Base64 编码
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
@Component
public class CipherManagerImpl implements CipherManager {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String AES_KEY = "u2oh6Vu^HWe4_AES";

    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec;

    public CipherManagerImpl() {
        byte[] keyBytes = AES_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = AES_KEY.getBytes(StandardCharsets.UTF_8);
        this.keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        this.ivSpec = new IvParameterSpec(ivBytes);
    }

    @Override
    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    @Override
    public String generateKey() {
        // 生成 16 字节的密钥（AES-128）
        byte[] keyBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    @Override
    public void setKey(String key) {
        // 不支持动态设置 key，因为 key 和 iv 必须相同
        throw new UnsupportedOperationException("Key cannot be set dynamically");
    }

    @Override
    public String getKey() {
        return AES_KEY;
    }
}
