package com.fangcang.titanjr.rs.request;


import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fangcang.titanjr.common.exception.RSValidateException;
import com.fangcang.titanjr.common.util.RequestValidationUtil;

/***
 * 上报企业资料信息
 * @author luoqinglong
 * @2016年11月1日
 */
public class OprsystemCreditCompanyRequest extends BaseRequest {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1451173831559843167L;

	/**
	 * 企业名称
	 */
	@NotNull
	private String companyname;
	/**
	 * 企业登记机关号
	 */
	private String registrationorga;
	/**
	 * 营业执照号
	 */
	@NotNull
	private String businesslicense;
	/**
	 * 营业照生效日期
	 */
	@NotNull
	private Date certificatestartdate;
	/**
	 * 营业照失效日期
	 */
	@NotNull
	private Date certificateexpiredate;
	/**
	 * 企业类型1.有限责任公司； 2.股份有限公司； 3.内资； 4.国有全资； 5.集体全资； 6.国外投资股份有限公司； 99.其它；
	 */
	@NotNull
	private Integer companytype;
	/**
	 * 注册资本
	 */
	@NotNull
	private String registfinance;
	/**
	 * 企业地址
	 */
	@NotNull
	private String address;
	/**
	 * 税务登记证号
	 */
	private String taxregcard;
	/**
	 * 组织机构代码证号
	 */
	private String organcertificate;
	/**
	 * 开户许可证号
	 */
	private String acuntopenlince;
	/**
	 * 法人姓名
	 */
	@NotNull
	private String corporatename;
	/**
	 * 法人证件类型 1.身份证; 2.护照; 8.户口本; 21.军官证; 22.士兵证; 23.回乡证; 24.台湾居民往来大陆通行证; 25.香港居民往来大陆通行证; 99.其他
	 */
	@NotNull
	private Integer certificatetype;
	/**
	 * 法人证件号码
	 */
	@NotNull
	private String certificatenumber;
	
	
	@Override
	public void check() throws RSValidateException {
		//校验不能为空
		RequestValidationUtil.check(this);
		RequestValidationUtil.checkNotEmpty(getUserid(), "userid");
	}

	
	public String getAcuntopenlince() {
		return acuntopenlince;
	}


	public void setAcuntopenlince(String acuntopenlince) {
		this.acuntopenlince = acuntopenlince;
	}


	 


	public String getCompanyname() {
		return companyname;
	}

	public void setCompanyname(String companyname) {
		this.companyname = companyname;
	}

	public String getBusinesslicense() {
		return businesslicense;
	}

	public void setBusinesslicense(String businesslicense) {
		this.businesslicense = businesslicense;
	}

	public String getRegistfinance() {
		return registfinance;
	}

	public void setRegistfinance(String registfinance) {
		this.registfinance = registfinance;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCorporatename() {
		return corporatename;
	}

	public void setCorporatename(String corporatename) {
		this.corporatename = corporatename;
	}

	public String getCertificatenumber() {
		return certificatenumber;
	}

	public void setCertificatenumber(String certificatenumber) {
		this.certificatenumber = certificatenumber;
	}

	public String getRegistrationorga() {
		return registrationorga;
	}

	public void setRegistrationorga(String registrationorga) {
		this.registrationorga = registrationorga;
	}

	public String getTaxregcard() {
		return taxregcard;
	}

	public void setTaxregcard(String taxregcard) {
		this.taxregcard = taxregcard;
	}

	public String getOrgancertificate() {
		return organcertificate;
	}

	public void setOrgancertificate(String organcertificate) {
		this.organcertificate = organcertificate;
	}


	public Date getCertificatestartdate() {
		return certificatestartdate;
	}


	public void setCertificatestartdate(Date certificatestartdate) {
		this.certificatestartdate = certificatestartdate;
	}


	public Date getCertificateexpiredate() {
		return certificateexpiredate;
	}


	public void setCertificateexpiredate(Date certificateexpiredate) {
		this.certificateexpiredate = certificateexpiredate;
	}


	public Integer getCompanytype() {
		return companytype;
	}


	public void setCompanytype(Integer companytype) {
		this.companytype = companytype;
	}


	public Integer getCertificatetype() {
		return certificatetype;
	}


	public void setCertificatetype(Integer certificatetype) {
		this.certificatetype = certificatetype;
	}

	
	
}
