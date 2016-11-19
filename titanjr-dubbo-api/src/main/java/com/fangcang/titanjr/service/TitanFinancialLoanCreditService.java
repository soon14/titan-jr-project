package com.fangcang.titanjr.service;

import com.fangcang.titanjr.dto.request.ApplyLoanCreditRequest;
import com.fangcang.titanjr.dto.request.AuditCreidtOrderRequest;
import com.fangcang.titanjr.dto.request.GetCreditInfoRequest;
import com.fangcang.titanjr.dto.request.LoanCreditSaveRequest;
import com.fangcang.titanjr.dto.request.QueryCreditCompanyInfoRequest;
import com.fangcang.titanjr.dto.response.ApplyLoanCreditResponse;
import com.fangcang.titanjr.dto.response.AuditCreidtOrderResponse;
import com.fangcang.titanjr.dto.response.GetCreditInfoResponse;
import com.fangcang.titanjr.dto.response.LoanCreditSaveResponse;
import com.fangcang.titanjr.dto.response.QueryCreditCompanyInfoResponse;

/**
 * 授信业务接口定义
 * 
 * @author wengxitao
 *
 */
public interface TitanFinancialLoanCreditService {
	
	/**
	 * 审核贷款授信单
	 * @param 审核对象
	 */
	public AuditCreidtOrderResponse auditCreditOrder(AuditCreidtOrderRequest req);
	
	/**
	 * 获取授信信息
	 * @param req 请求对象
	 * @return
	 */
	public GetCreditInfoResponse getCreditOrderInfo(GetCreditInfoRequest req);
	
	/**
	 * 查询授信申请公司信息
	 * @return
	 */
	public QueryCreditCompanyInfoResponse queryCreditCompanyInfo(QueryCreditCompanyInfoRequest req);
	/**
	 * 保存授信单信息
	 * 1.判定orderNo，若为空，则生成编码并保存，不为空，则更新
	 * 2.LoanCreditCompany中的controllDatas做了调整，方便存储；
	 * @param req 请求对象
	 * @return
	 */
	public LoanCreditSaveResponse saveCreditOrder(LoanCreditSaveRequest req);

	/**
	 * 授信申请
	 * 
	 * @param req  授信对象
	 * @return
	 */
	public ApplyLoanCreditResponse applyLoanCredit(ApplyLoanCreditRequest req);

	

}
