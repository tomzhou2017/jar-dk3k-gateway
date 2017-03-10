package com.dk3k.framework.hbase.support;

/**
 * @author lait.zhang@gmail.com
 * @Date 20161227
 * @Description:<code>可加解密接口</code>
 */
public interface EncryptAndDecryptAble {
    void encrypt(Object obj) throws Throwable;

    void decrypt(Object obj) throws Throwable;
}
