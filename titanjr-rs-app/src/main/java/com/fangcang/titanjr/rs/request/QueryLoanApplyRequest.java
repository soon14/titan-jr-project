package com.fangcang.titanjr.rs.request;

import javax.validation.constraints.NotNull;

import com.fangcang.titanjr.common.exception.RSValidateException;
import com.fangcang.titanjr.common.util.RequestValidationUtil;
import com.fangcang.util.StringUtil;

/**
 * 查询贷款订单状态
 * @author luoqinglong
 * @2016年11月8日
 */
public class QueryLoanApplyRequest extends BaseRequest {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5044257704655215581L;
	/**
	 * 	机构码
	 */
	@NotNull
	private String rootinstcd;
	/**
	 * 	商户id
	 */
	@NotNull
	private String userorderid;
	
	
	@Override
	public void check() throws RSValidateException {
		RequestValidationUtil.checkNotEmpty(this.getUserid(), "userid");
		RequestValidationUtil.check(this);
	}
	/**
	 * 兼容父类的constid
	 * @return
	 */
	public String getRootinstcd() {
		if(StringUtil.isValidString(rootinstcd)){
			return rootinstcd;
		}
		return getConstid();
	}

	public void setRootinstcd(String rootinstcd) {
		this.setConstid(rootinstcd);
		this.rootinstcd = rootinstcd;
	}

	public String getUserorderid() {
		return userorderid;
	}

	public void setUserorderid(String userorderid) {
		this.userorderid = userorderid;
	}
}
