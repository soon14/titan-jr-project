/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName QuickCardHistory.java
 * @author Jerry
 * @date 2017年8月8日 上午9:43:03  
 */
package com.fangcang.titanjr.dto.bean.gateway;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Jerry
 * @date 2017年8月8日 上午9:43:03  
 */
public class QuickCardHistoryDTO implements Serializable {

	/** 
	 * 
	 */
	private static final long serialVersionUID = -4011062490067624120L;
	
	/**
	 * 主键
	 */
	private int quickcardid;
	/**
	 * 所属机构
	 */
	@NotBlank
	private String orgcode;
	/**
	 * 第三方用户ID
	 */
	@NotBlank
	private String fcuserid;
	/**
	 * 支付人姓名
	 */
	@NotBlank
	private String payername;
	/**
	 * 支付人银行卡号
	 */
	@NotBlank
	private String payeracount;
	/**
	 * 银行名称
	 */
	@NotBlank
	private String bankname;
	/**
	 * 银行标识
	 */
	@NotBlank
	private String bankinfo;
	/**
	 * 支付人银行卡类型   10：借记卡  11：信用卡
	 */
	@NotBlank
	private String payeraccounttype;
	/**
	 * 支付人手机号
	 */
	@NotBlank
	private String payerphone;
	/**
	 * 证件类型：1身份证
	 */
	@NotBlank
	private String idtype;
	/**
	 * 支付人证件号
	 */
	@NotBlank
	private String idcode;
	/**
	 * 信用卡背后的3位数字
	 */
	private String safetycode;
	/**
	 * 有效期（信用卡）：月年格式 例如2020年09月应写为0920
	 */
	private String validthru;
	/**
	 * 创建人
	 */
	private String creator;
	/**
	 * 创建时间
	 */
	private String createtime;
	/**
	 * 使用次数
	 */
	private int count;
	/**
	 * 最近一次使用时间
	 */
	private String lasttime;

	public int getQuickcardid() {
		return quickcardid;
	}

	public void setQuickcardid(int quickcardid) {
		this.quickcardid = quickcardid;
	}

	public String getOrgcode() {
		return orgcode;
	}

	public void setOrgcode(String orgcode) {
		this.orgcode = orgcode;
	}

	public String getFcuserid() {
		return fcuserid;
	}

	public void setFcuserid(String fcuserid) {
		this.fcuserid = fcuserid;
	}

	public String getPayername() {
		return payername;
	}

	public void setPayername(String payername) {
		this.payername = payername;
	}

	public String getPayeracount() {
		return payeracount;
	}

	public void setPayeracount(String payeracount) {
		this.payeracount = payeracount;
	}

	public String getBankname() {
		return bankname;
	}

	public void setBankname(String bankname) {
		this.bankname = bankname;
	}

	public String getBankinfo() {
		return bankinfo;
	}

	public void setBankinfo(String bankinfo) {
		this.bankinfo = bankinfo;
	}

	public String getPayeraccounttype() {
		return payeraccounttype;
	}

	public void setPayeraccounttype(String payeraccounttype) {
		this.payeraccounttype = payeraccounttype;
	}

	public String getPayerphone() {
		return payerphone;
	}

	public void setPayerphone(String payerphone) {
		this.payerphone = payerphone;
	}

	public String getIdtype() {
		return idtype;
	}

	public void setIdtype(String idtype) {
		this.idtype = idtype;
	}

	public String getIdcode() {
		return idcode;
	}

	public void setIdcode(String idcode) {
		this.idcode = idcode;
	}

	public String getSafetycode() {
		return safetycode;
	}

	public void setSafetycode(String safetycode) {
		this.safetycode = safetycode;
	}

	public String getValidthru() {
		return validthru;
	}

	public void setValidthru(String validthru) {
		this.validthru = validthru;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getLasttime() {
		return lasttime;
	}

	public void setLasttime(String lasttime) {
		this.lasttime = lasttime;
	}

}
