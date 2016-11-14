package com.fangcang.titanjr.rs.request;

import javax.validation.constraints.NotNull;

import com.fangcang.titanjr.common.exception.RSValidateException;
import com.fangcang.titanjr.common.util.RequestValidationUtil;
import com.fangcang.util.StringUtil;

/**
 * 查询贷款的还款状态及历史
 * @author luoqinglong
 * @2016年11月8日
 */
public class QueryUserRepaymentRequest extends BaseRequest {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6814381718160525529L;

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
		RequestValidationUtil.checkNotEmpty(getProductid(), "productid");
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
