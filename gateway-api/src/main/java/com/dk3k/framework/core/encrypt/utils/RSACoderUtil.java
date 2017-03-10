package com.dk3k.framework.core.encrypt.utils;

import com.dk3k.framework.core.encrypt.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSACoderUtil extends CoderUtil {
	public static final String KEY_ALGORTHM = "RSA";
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

	public static final String PUBLIC_KEY = "RSAPublicKey"; // 公钥
	public static final String PRIVATE_KEY = "RSAPrivateKey"; // 私钥

	/** RSA最大加密明文大小 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/** RSA最大解密密文大小 */
	private static final int MAX_DECRYPT_BLOCK = 128;

	public static String encryptByPrivateKey(String contentStr, String key) throws Exception {
		byte[] bb = RSACoderUtil.encryptByPrivateKey(contentStr.getBytes(), key);
		return Base64.encode(bb);
	}

	public static String decryptByPublicKey(String base64Data, String key) throws Exception {
		if (StringUtils.isBlank(base64Data)) {
			return "";
		}
		byte[] contentBytes = Base64.decode(base64Data);
		byte[] stringBytes = RSACoderUtil.decryptByPublicKey(contentBytes, key);
		return new String(stringBytes);
	}

	/**
	 * 用私钥加密
	 *
	 * @param data
	 *            加密数据
	 * @param key
	 *            密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
		// 解密密钥
		byte[] keyBytes = decryptBASE64(key);
		// 取私钥
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

		// 对数据加密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);

		// 加密时超过117字节就报错。为此采用分段加密的办法来加密
		byte[] dataReturn = null;
		for (int i = 0; i < data.length; i += MAX_ENCRYPT_BLOCK) {
			byte[] doFinal = cipher.doFinal(RSACoderUtil.subarray(data, i, i + MAX_ENCRYPT_BLOCK));
			dataReturn = RSACoderUtil.addAll(dataReturn, doFinal);
		}

		return dataReturn;
	}

	/**
	 * 用私钥解密
	 *
	 * @param data
	 *            加密数据
	 * @param key
	 *            密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, String key) throws Exception {
		// 对私钥解密
		byte[] keyBytes = decryptBASE64(key);

		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		// 对数据解密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		// 解密时超过128字节就报错。为此采用分段解密的办法来解密
		byte[] dataReturn = null;
		for (int i = 0; i < data.length; i += MAX_DECRYPT_BLOCK) {
			byte[] doFinal = cipher.doFinal(RSACoderUtil.subarray(data, i, i + MAX_DECRYPT_BLOCK));
			dataReturn = RSACoderUtil.addAll(dataReturn, doFinal);
		}
		return dataReturn;
	}

	/**
	 * 用公钥加密
	 *
	 * @param data
	 *            加密数据
	 * @param key
	 *            密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, String key) throws Exception {
		// 对公钥解密
		byte[] keyBytes = decryptBASE64(key);
		// 取公钥
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

		// 对数据解密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		// 加密时超过117字节就报错。为此采用分段加密的办法来加密
		byte[] dataReturn = null;
		for (int i = 0; i < data.length; i += MAX_ENCRYPT_BLOCK) {
			byte[] doFinal = cipher.doFinal(RSACoderUtil.subarray(data, i, i + MAX_ENCRYPT_BLOCK));
			dataReturn = RSACoderUtil.addAll(dataReturn, doFinal);
		}

		return dataReturn;
	}

	/**
	 * 用公钥解密
	 *
	 * @param data
	 *            加密数据
	 * @param key
	 *            密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(byte[] data, String key) throws Exception {
		// 对私钥解密
		byte[] keyBytes = decryptBASE64(key);
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

		// 对数据解密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);

		// 解密时超过128字节就报错。为此采用分段解密的办法来解密
		byte[] dataReturn = null;
		for (int i = 0; i < data.length; i += MAX_DECRYPT_BLOCK) {
			byte[] doFinal = cipher.doFinal(RSACoderUtil.subarray(data, i, i + MAX_DECRYPT_BLOCK));
			dataReturn = RSACoderUtil.addAll(dataReturn, doFinal);
		}

		return dataReturn;

	}

	/**
	 * 用私钥对信息生成数字签名
	 *
	 * @param data
	 *            加密数据
	 * @param privateKey
	 *            私钥
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] data, String privateKey) throws Exception {
		// 解密私钥
		byte[] keyBytes = decryptBASE64(privateKey);
		// 构造PKCS8EncodedKeySpec对象
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		// 指定加密算法
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		// 取私钥匙对象
		PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

		// 用私钥对信息生成数字签名
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateKey2);
		signature.update(data);

		return encryptBASE64(signature.sign());
	}

	/**
	 * 校验数字签名
	 *
	 * @param data
	 *            加密数据
	 * @param publicKey
	 *            公钥
	 * @param sign
	 *            数字签名
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
		// 解密公钥
		byte[] keyBytes = decryptBASE64(publicKey);
		// 构造X509EncodedKeySpec对象
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		// 指定加密算法
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
		// 取公钥匙对象
		PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);

		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicKey2);
		signature.update(data);
		// 验证签名是否正常
		return signature.verify(decryptBASE64(sign));
	}

	/**
	 * <p>
	 * Produces a new <code>byte</code> array containing the elements between
	 * the start and end indices.
	 * </p>
	 *
	 * <p>
	 * The start index is inclusive, the end index exclusive. Null array input
	 * produces null output.
	 * </p>
	 *
	 * @param array
	 *            the array
	 * @param startIndexInclusive
	 *            the starting index. Undervalue (&lt;0) is promoted to 0,
	 *            overvalue (&gt;array.length) results in an empty array.
	 * @param endIndexExclusive
	 *            elements up to endIndex-1 are present in the returned
	 *            subarray. Undervalue (&lt; startIndex) produces empty array,
	 *            overvalue (&gt;array.length) is demoted to array length.
	 * @return a new array containing the elements between the start and end
	 *         indices.
	 * @since 2.1
	 */
	public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
		if (array == null) {
			return null;
		}
		if (startIndexInclusive < 0) {
			startIndexInclusive = 0;
		}
		if (endIndexExclusive > array.length) {
			endIndexExclusive = array.length;
		}
		int newSize = endIndexExclusive - startIndexInclusive;
		if (newSize <= 0) {
			return new byte[0];
		}

		byte[] subarray = new byte[newSize];
		System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
		return subarray;
	}

	/**
	 * <p>
	 * Adds all the elements of the given arrays into a new array.
	 * </p>
	 * <p>
	 * The new array contains all of the element of <code>array1</code> followed
	 * by all of the elements <code>array2</code>. When an array is returned, it
	 * is always a new array.
	 * </p>
	 *
	 * <pre>
	 * ArrayUtils.addAll(array1, null)   = cloned copy of array1
	 * ArrayUtils.addAll(null, array2)   = cloned copy of array2
	 * ArrayUtils.addAll([], [])         = []
	 * </pre>
	 *
	 * @param array1
	 *            the first array whose elements are added to the new array.
	 * @param array2
	 *            the second array whose elements are added to the new array.
	 * @return The new byte[] array.
	 * @since 2.1
	 */
	public static byte[] addAll(byte[] array1, byte[] array2) {
		if (array1 == null) {
			return clone(array2);
		} else if (array2 == null) {
			return clone(array1);
		}
		byte[] joinedArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * <p>
	 * Clones an array returning a typecast result and handling
	 * <code>null</code>.
	 * </p>
	 *
	 * <p>
	 * This method returns <code>null</code> for a <code>null</code> input
	 * array.
	 * </p>
	 *
	 * @param array
	 *            the array to clone, may be <code>null</code>
	 * @return the cloned array, <code>null</code> if <code>null</code> input
	 */
	public static byte[] clone(byte[] array) {
		if (array == null) {
			return null;
		}
		return array.clone();
	}

	public static void main(String[] args) throws Exception {
		String key1 = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIuMmzVUHjndqZ1mhKMli4Lc31CMjOq/5LR3eG6BlZVmi1/DOI1TbLE1Dm6MeUMRw5zsbe4fwcZMCvWWDW9eTTZOLzvO/h9W/TR2suyByElpI4B0bBtQUQg2VlIoADZpojoQES0V58HWfAB1MGxMby4QZuGezGh5GT33Ku7wOhnPAgMBAAECgYBewk2qesJDnOdOQI0Uk6wWYpEpiyctSLyuZunacBf46Tb/AftmzB1kf5ibLLwy9N8Vsd478kiMtJpEhW3+D9TxZ94mR0v+PPuEUYJI7vD9bUxrz3opTke241DBDFl1q6oWf8trpojMaPHwbwewYRjJoqj+yTtkgDFQ2iPT/jcq4QJBAMvK98gJKhq8jjHcmL8ttxCtEmBA5PQyBx+qtgZqUAqVaCFo6TZHNTlUkD+jMyR4dSxq8CITQFKIbiq90JTuV8cCQQCvTHMHWLLGurYMSUK2zM0CJvgp4QvD8uMQ2c/xniwERrZ4YqOzJVXxcDwS8ZAJwZNx07CtexmQh1k39eP02/25AkEAtF5Lt4wE6+z+nTr1Jh+76tKDdRV6jnbIv0pEGKF3fiaGJyvHXPbhMICbuvciiKDkdA+hwUbwoxuFBedB+M6KeQJADVmWHQYdP1My0Aa4RPGd3z8WgSH5YmfZ8QuVcYIpOf4koVBpxRffzq5MdOjenk+WqgGEIAIS9VXJbqCURq6GSQJAcz/WAXdvFC4gnNz98aJq1USVYgJoRLNA0qoxBl/4GJ45HkhjZq9h8a3gBt0h476/h554mH9Y+1tlSgm2OgPDQQ==";
		String content = "哈MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKm/4UqBUJpEvIZ1ru/Qy45/rYbLF8bEy+t0s3F6/Oz1ZDSDvQf4rHZRamlo9ehSKZOn3GoYzYUfRX0mxJLQnJ6jNjvowdLz17ROUqdb2XI65ada1GQ9QzCnLouBx+EjVGtKiD9asX5IaR+JIY66xiBfe0presOtbRt1x6jySZF3AgMBAAECgYBs2QV16QXuZjUdY0kRrilY7rYuwPw2EOtRyDtogRy+NXxT0EXeXq74D6zUeuF0X3sZm4HS5qSiwDKm1b0nB23lmz0Zgs2FfOTUeF0unGDAPclENT+4QHfD1R+oKfx2Jb4rnXKNwEW0o6+N0LUEfK88iGTBXBqoksrIeUdm9myFQQJBAPwzVNOEd4FqvTL/GzPJYVjB8po6VdNrUSCwCZixD5NcsN19S3TDv4eoYYWSYO/EZqLD6hJB1LIIYtd50dYKVFUCQQCsTo7HBwRYMU50jGCtHFGccMqLpxVhm2quF/zOyxXG0OEAVAjJsMwx78LvOE2bIJEYYJIA19OoYP9bAMpOeHqbAkEA5WZbljo76cAJcMWTHLvkjDN+VMMTJkfLd4wobnc/z67UQeOiKweSw/ZpHnJTMkTXht3ln825hmcSYSUxtRuYMQJANcLNCYxG7r1znSmvgFO1ovoOOESJh0rWnjsdxefXg5DooxGVLFjdXTdRuxH2JNCkdNast1af04lwIxS8Cn/MwQJAN5vdy+1IqtOIg3ian8ccvFd+iKbp5n19hZa7ilJiVXU6sxtuYM6BVm0+LTcLAL2KxfA+OElkkMZ4XhqPxexN3w=";
		String con2 = "123";
		String key2 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCLjJs1VB453amdZoSjJYuC3N9QjIzqv+S0d3hugZWVZotfwziNU2yxNQ5ujHlDEcOc7G3uH8HGTAr1lg1vXk02Ti87zv4fVv00drLsgchJaSOAdGwbUFEINlZSKAA2aaI6EBEtFefB1nwAdTBsTG8uEGbhnsxoeRk99yru8DoZzwIDAQAB";
		// byte[] byte64 = Base64Util.base64ToByteArray(content);
		// String responseResult = new String(
		// RSACoderUtil.decryptByPrivateKey(byte64, key1), "utf-8");
		System.out.println(new String(RSACoderUtil.decryptByPublicKey(RSACoderUtil.encryptByPrivateKey(content.getBytes(), key1), key2)));

	}
}
