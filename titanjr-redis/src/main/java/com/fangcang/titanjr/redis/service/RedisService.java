package com.fangcang.titanjr.redis.service;

import java.util.concurrent.TimeUnit;


/**
 * 缓存
 * @author luoqinglong
 *
 */
public interface RedisService {
	
	/***
	 * 保存redis值
	 * @param key 业务key, 格式为：username ,最后合成的key 为：TITANJR:com-fangcang-titanjr-common-util-Wxutil:username
	 * @param value
	 */
	public void setValue(String key,Object value);
	
	/**
	 * 保存redis值
	 * @param key 业务key, 格式为：username ,最后合成的key 为：TITANJR:com-fangcang-titanjr-common-util-Wxutil:username
	 * @param value
	 * @param timeout 失效时间，单位：分钟
	 */
	public void setValue(String key,Object value,long timeout);
	
	
	/**
	 * 保存redis值
	 * @param key 业务key, 格式为：username ,最后合成的key 为：TITANJR:com-fangcang-titanjr-common-util-Wxutil:username
	 * @param value
	 * @param timeout 失效时间
	 * @param unit 失效时间单位
	 */
	public void setValue(String key,Object value,long timeout,TimeUnit unit);
	/***
	 * 
	 * @param key 业务key, 格式为：username ,最后合成的key 为：TITANJR:com-fangcang-titanjr-common-util-Wxutil:username
	 * @return
	 */
	public Object getValue(String key);
	
	/**
	 * 删除 某个值
	 * @param key 业务key, 格式为：username ,最后合成的key 为：TITANJR:com-fangcang-titanjr-common-util-Wxutil:username
	 */
	public void deleteValue(String key);
	
	/***
	 * 获取锁
	 * @param key 锁键
	 * @return 锁值
	 */
	public boolean lock(String key);
	
}
