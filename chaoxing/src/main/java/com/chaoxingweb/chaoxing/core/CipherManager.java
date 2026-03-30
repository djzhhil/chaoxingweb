package com.chaoxingweb.chaoxing.core;

/**
 * 加密管理器接口
 *
 * @author 小克 🐕💎
 * @since 2026-03-30
 */
public interface CipherManager {

    /**
     * 加密
     *
     * @param plaintext 明文
     * @return 密文
     */
    String encrypt(String plaintext);

    /**
     * 解密
     *
     * @param ciphertext 密文
     * @return 明文
     */
    String decrypt(String ciphertext);

    /**
     * 生成密钥
     *
     * @return 密钥
     */
    String generateKey();

    /**
     * 设置密钥
     *
     * @param key 密钥
     */
    void setKey(String key);

    /**
     * 获取密钥
     *
     * @return 密钥
     */
    String getKey();
}
