package com.dk3k.framework.core.encrypt.utils;

import com.dk3k.framework.core.encrypt.MD5;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 签名工具类
 * </p>
 *
 * @author qiuyangjun
 */
public class SignUtil {
    private final static String SIGN_KEY_MD5 = "jzw_(07wAm~XSE1W+91(i@bVJo*=WXZt<7";

    public final static String INPUT_CHARSET = "UTF-8";//参数编码字符集


    /**
     * 签名
     *
     * @param content 签名内容
     * @return
     * @throws Exception
     */
    public static String sign(String content) throws Exception {
        return sign(content, SIGN_KEY_MD5);
    }

    /**
     * 签名
     *
     * @param content 签名内容
     * @param signKey 密钥
     * @return
     * @throws Exception
     */
    public static String sign(String content, String signKey) throws Exception {
        String key = signKey;
        if (StringUtils.isBlank(key)) {
            key = SIGN_KEY_MD5;
        }
        return sign(content, key, INPUT_CHARSET);
    }

    /**
     * 签名
     *
     * @param content 签名内容
     * @param signKey 密钥
     * @param charset 字符集
     * @return
     * @throws Exception
     */
    public static String sign(String content, String signKey, String charset) throws Exception {
        String key = signKey;
        if (StringUtils.isBlank(key)) {
            key = SIGN_KEY_MD5;
        }
        String inputCharset = charset;
        if (StringUtils.isBlank(inputCharset)) {
            inputCharset = INPUT_CHARSET;
        }
        return MD5.sign(content, key, inputCharset);
    }

    /**
     * 验证签名
     *
     * @param content 签名内容
     * @param signMsg 网关返回签名
     * @return
     * @throws Exception
     */
    public static boolean checkSign(String content, String signMsg) throws Exception {
        return checkSign(content, signMsg, SIGN_KEY_MD5);
    }


    /**
     * 验证签名
     *
     * @param content 签名内容
     * @param signMsg 网关返回签名
     * @param signKey 密钥
     * @return
     * @throws Exception
     */
    public static boolean checkSign(String content, String signMsg, String signKey) throws Exception {
        String key = signKey;
        if (StringUtils.isBlank(key)) {
            key = SIGN_KEY_MD5;
        }
        return checkSign(content, signMsg, key, INPUT_CHARSET);
    }

    /**
     * 验证签名
     *
     * @param content 签名内容
     * @param signMsg 网关返回签名
     * @param signKey 密钥
     * @param charset 字符集
     * @return
     * @throws Exception
     */
    public static boolean checkSign(String content, String signMsg, String signKey, String charset) throws Exception {
        String key = signKey;
        if (StringUtils.isBlank(key)) {
            key = SIGN_KEY_MD5;
        }
        String inputCharset = charset;
        if (StringUtils.isBlank(inputCharset)) {
            inputCharset = INPUT_CHARSET;
        }
        return MD5.verify(content, signMsg, key, inputCharset);
    }


}
