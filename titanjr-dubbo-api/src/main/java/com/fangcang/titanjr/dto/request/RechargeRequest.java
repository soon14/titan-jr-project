package com.fangcang.titanjr.dto.request;

import java.io.Serializable;

public class RechargeRequest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String userid;
	//商户号
	private String merchantNo;
	//业务订单号
	private String orderNo;
	//产品号
	private String productNo;
	//产品名称
	private String productName;
	//支付订单时间
	private String productDesc;
	//产品数量
	private String productNum;
	//订单金额
	private String orderAmount;
	//支付方式
	private String payType;
	//币种
	private String amtType;
	//银行标识
	private String bankInfo;
	//支付人账号
	private String payerAcount;
	//支付人姓名
	private String payerName;
	//支付人手机号
	private String payerPhone;
	//支付人邮箱
	private String payerMail;
	//页面返回地址
	private String pageUrl;
	//结果通讯地址
	private String notifyUrl;
	//订单提交时间
	private String orderTime;
	//订单过期时间
	private Integer orderExpireTime;
	//订单标志
	private String orderMark;
	//扩展字段
	private String expand;
	//扩展字段2
	private String expand2;
	//签名类型
	private String signType;
	//业务号
	private String busiCode;
	//版本
	private String version="v1.0";
	//编码
	private String charset;
	//加密的秘钥
	private String key = "12356780Poi";
	
	//落单生成的Id
	private Integer transorderid;
	
	// 应收手续费
	private Long receivablefee;
	// 实收手续费
	private Long receivedfee;
	// 标准费率手续费
	private Long standfee;
	private Float standardrate;
	private Float executionrate;
	private Integer ratetype;
	
	//新版收银台增加字段
	private String idCode; //支付人身份证
	private String payerAccountType; //支付人银行卡类型   10：借记卡  21：信用卡
	private String safetyCode;//信用卡背后的3位数字
	private String validthru;//月年格式 例如2020年09月应写为0920
	
	public Integer getRatetype() {
		return ratetype;
	}
	public void setRatetype(Integer ratetype) {
		this.ratetype = ratetype;
	}
	
	public Long getReceivablefee() {
		return receivablefee;
	}
	public void setReceivablefee(Long receivablefee) {
		this.receivablefee = receivablefee;
	}
	public Long getReceivedfee() {
		return receivedfee;
	}
	public void setReceivedfee(Long receivedfee) {
		this.receivedfee = receivedfee;
	}
	public Long getStandfee() {
		return standfee;
	}
	public void setStandfee(Long standfee) {
		this.standfee = standfee;
	}
	public Float getStandardrate() {
		return standardrate;
	}
	public void setStandardrate(Float standardrate) {
		this.standardrate = standardrate;
	}
	public Float getExecutionrate() {
		return executionrate;
	}
	public void setExecutionrate(Float executionrate) {
		this.executionrate = executionrate;
	}
	public Integer getTransorderid() {
		return transorderid;
	}
	public void setTransorderid(Integer transorderid) {
		this.transorderid = transorderid;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getMerchantNo() {
		return merchantNo;
	}
	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getProductNo() {
		return productNo;
	}
	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getProductDesc() {
		return productDesc;
	}
	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}
	public String getProductNum() {
		return productNum;
	}
	public void setProductNum(String productNum) {
		this.productNum = productNum;
	}
	public String getOrderAmount() {
		return orderAmount;
	}
	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public String getAmtType() {
		return amtType;
	}
	public void setAmtType(String amtType) {
		this.amtType = amtType;
	}
	public String getBankInfo() {
		return bankInfo;
	}
	public void setBankInfo(String bankInfo) {
		this.bankInfo = bankInfo;
	}
	public String getPayerAcount() {
		return payerAcount;
	}
	public void setPayerAcount(String payerAcount) {
		this.payerAcount = payerAcount;
	}
	public String getPayerName() {
		return payerName;
	}
	public void setPayerName(String payerName) {
		this.payerName = payerName;
	}
	public String getPayerPhone() {
		return payerPhone;
	}
	public void setPayerPhone(String payerPhone) {
		this.payerPhone = payerPhone;
	}
	public String getPayerMail() {
		return payerMail;
	}
	public void setPayerMail(String payerMail) {
		this.payerMail = payerMail;
	}
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	public Integer getOrderExpireTime() {
		return orderExpireTime;
	}
	public void setOrderExpireTime(Integer orderExpireTime) {
		this.orderExpireTime = orderExpireTime;
	}
	public String getOrderMark() {
		return orderMark;
	}
	public void setOrderMark(String orderMark) {
		this.orderMark = orderMark;
	}
	public String getExpand() {
		return expand;
	}
	public void setExpand(String expand) {
		this.expand = expand;
	}
	public String getExpand2() {
		return expand2;
	}
	public void setExpand2(String expand2) {
		this.expand2 = expand2;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getBusiCode() {
		return busiCode;
	}
	public void setBusiCode(String busiCode) {
		this.busiCode = busiCode;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getIdCode() {
		return idCode;
	}
	public void setIdCode(String idCode) {
		this.idCode = idCode;
	}
	public String getPayerAccountType() {
		return payerAccountType;
	}
	public void setPayerAccountType(String payerAccountType) {
		this.payerAccountType = payerAccountType;
	}
	public String getSafetyCode() {
		return safetyCode;
	}
	public void setSafetyCode(String safetyCode) {
		this.safetyCode = safetyCode;
	}
	public String getValidthru() {
		return validthru;
	}
	public void setValidthru(String validthru) {
		this.validthru = validthru;
	}
}
