/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName TLOrderRefundService.java
 * @author Jerry
 * @date 2017年12月4日 下午2:39:44  
 */
package com.titanjr.checkstand.service;

import com.titanjr.checkstand.request.TLNetBankOrderRefundRequest;
import com.titanjr.checkstand.request.TLQrOrderRefundRequest;
import com.titanjr.checkstand.respnse.TitanOrderRefundResponse;

/**
 * @author Jerry
 * @date 2017年12月4日 下午2:39:44  
 */
public interface TLOrderRefundService {
	
	/**
	 * 通联网银支付订单退款
	 * @author Jerry
	 * @date 2017年12月4日 下午2:41:28
	 */
	public TitanOrderRefundResponse netBankOrderRefund(TLNetBankOrderRefundRequest 
			tlNetBankOrderRefundRequest);
	
	
	/**
	 * 通联扫码支付订单撤销（当天）/退款（隔天后）
	 * @author Jerry
	 * @date 2017年12月19日 下午7:34:53
	 */
	public TitanOrderRefundResponse qrCodeOrderRefund(TLQrOrderRefundRequest tlQrOrderRefundRequest);

}