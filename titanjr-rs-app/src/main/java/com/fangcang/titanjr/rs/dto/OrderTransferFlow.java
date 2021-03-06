package com.fangcang.titanjr.rs.dto;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
@XStreamAlias("wheatfield_order_service_multitransfer_query_response")
public class OrderTransferFlow implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    private String is_success;
	
	private String msg;
	
	//返回码
	private String retcode;
	
	//返回信息
	private String retmsg;
	
	//查询到的返回结果
	@XStreamAlias("tradeinfos")
	private TradeInfoList tradeInfoList;

	public String getRetcode() {
		return retcode;
	}

	public void setRetcode(String retcode) {
		this.retcode = retcode;
	}

	public String getRetmsg() {
		return retmsg;
	}

	public void setRetmsg(String retmsg) {
		this.retmsg = retmsg;
	}

	public TradeInfoList getTradeInfoList() {
		return tradeInfoList;
	}

	public void setTradeInfoList(TradeInfoList tradeInfoList) {
		this.tradeInfoList = tradeInfoList;
	}

	public String getIs_success() {
		return is_success;
	}

	public void setIs_success(String is_success) {
		this.is_success = is_success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
