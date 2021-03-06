package com.fangcang.titanjr.common.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/***
 * AES加密
 * @author luoqinglong
 * @date   2017年2月23日
 */
public class AESUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(AESUtil.class);
	public static String AES_SEED = "VenTPrgtBSYQBBSbWoqWViUspXoGgCkV";
	private final static int digit = 128;
	/**
	 * 字节转16进制
	 * 
	 * @param 二进制数组
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs;
	}
	
	/**
	 * 字符串转16进制
	 * @param hexStr 16进制字符串
	 * @return
	 */
	public static byte[] hex2byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
					16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}
	
	/**
	 * 加密 
	 * @param content明文
	 * @return 密文
	 */
	public static String encrypt(String content){
		return encrypt(content,AES_SEED);
	}

	/**
	 * 加密
	 * @param content 明文
	 * @param seed 种子
	 * @return
	 */
	public static String encrypt(String content, String seed) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );//解决在linux下每次加密结果都不一样问题.
	        secureRandom.setSeed(seed.getBytes()); 
			kgen.init(digit, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return byte2hex(result); // 加密
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("加密失败，参数content："+content+",NoSuchAlgorithmException",e);
		} catch (NoSuchPaddingException e) {
			LOGGER.error("加密失败，参数content："+content+",NoSuchPaddingException",e);
		} catch (InvalidKeyException e) {
			LOGGER.error("加密失败，参数content："+content+",InvalidKeyException",e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("加密失败，参数content："+content+",UnsupportedEncodingException",e);
		} catch (IllegalBlockSizeException e) {
			LOGGER.error("加密失败，参数content："+content+",IllegalBlockSizeException",e);
		} catch (BadPaddingException e) {
			LOGGER.error("加密失败，参数content："+content+",BadPaddingException",e);
		}

		return null;

	}
	
	/**
	 * 解密
	 * @param content 密文
	 * @return 明文
	 */
	public static String decrypt(String content) {
		return decrypt(content,AES_SEED);
	}
	/**
	 * 解密
	 * 
	 * @param content 密文
	 * @param seed aes种子
	 * @return 明文
	 */
	public static String decrypt(String content, String seed) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" ); 
	        secureRandom.setSeed(seed.getBytes()); 
			kgen.init(digit, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(hex2byte(content));
			
			return new String(result); // 解密
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("解密失败，参数密文content："+content+",NoSuchAlgorithmException",e);
		} catch (NoSuchPaddingException e) {
			LOGGER.error("解密失败，参数密文content："+content+",NoSuchPaddingException",e);
		} catch (InvalidKeyException e) {
			LOGGER.error("解密失败，参数密文content："+content+",InvalidKeyException",e);
		} catch (IllegalBlockSizeException e) {
			LOGGER.error("解密失败，参数密文content："+content+",IllegalBlockSizeException",e);
		} catch (BadPaddingException e) {
			LOGGER.error("解密失败，参数密文content："+content+",BadPaddingException",e);
		}
		return null;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 需要加密的内容
		String content = "10141";
		// 加密密码
		//String key = RandomStringUtils.randomAlphabetic(32);
		System.out.println("content：" + content);
		String en = AESUtil.encrypt(content);
		System.out.println("加密后密文：" + en);
		String de = AESUtil.decrypt(en);

		System.out.println("解密后明文：" + de);
	}
}
