package com.fangcang.titanjr.service.impl;

import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fangcang.corenut.dao.PaginationSupport;
import com.fangcang.titanjr.common.enums.FileTypeEnum;
import com.fangcang.titanjr.common.enums.LoanApplyOrderEnum;
import com.fangcang.titanjr.common.enums.LoanOrderStatusEnum;
import com.fangcang.titanjr.common.enums.LoanProductEnum;
import com.fangcang.titanjr.common.enums.entity.TitanOrgEnum;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.DateUtil;
import com.fangcang.titanjr.common.util.FileHelp;
import com.fangcang.titanjr.common.util.FtpUtil;
import com.fangcang.titanjr.common.util.JsonConversionTool;
import com.fangcang.titanjr.common.util.NumberUtil;
import com.fangcang.titanjr.common.util.SMSTemplate;
import com.fangcang.titanjr.common.util.Tools;
import com.fangcang.titanjr.dao.LoanCreditOrderDao;
import com.fangcang.titanjr.dao.LoanOrderDao;
import com.fangcang.titanjr.dao.LoanSpecificationDao;
import com.fangcang.titanjr.dao.PlatformOrderOprDao;
import com.fangcang.titanjr.dao.TitanTransOrderDao;
import com.fangcang.titanjr.dao.TitanVirtualOrgDao;
import com.fangcang.titanjr.dto.bean.LoanApplyOrderBean;
import com.fangcang.titanjr.dto.bean.LoanRepaymentBean;
import com.fangcang.titanjr.dto.bean.LoanRoomPackSpecBean;
import com.fangcang.titanjr.dto.bean.LoanSpecBean;
import com.fangcang.titanjr.dto.bean.LoanSpecificationBean;
import com.fangcang.titanjr.dto.bean.NewLoanApplyJsonDataBean;
import com.fangcang.titanjr.dto.bean.OrgDTO;
import com.fangcang.titanjr.dto.bean.OrgLoanStatInfo;
import com.fangcang.titanjr.dto.request.ApplyLoanRequest;
import com.fangcang.titanjr.dto.request.CancelLoanRequest;
import com.fangcang.titanjr.dto.request.ConfirmLoanOrderIsAvailableRequest;
import com.fangcang.titanjr.dto.request.GetHistoryRepaymentListRequest;
import com.fangcang.titanjr.dto.request.GetLoanOrderInfoListRequest;
import com.fangcang.titanjr.dto.request.GetLoanOrderInfoRequest;
import com.fangcang.titanjr.dto.request.GetOrgLoanStatInfoRequest;
import com.fangcang.titanjr.dto.request.LoanOrderNotifyRequest;
import com.fangcang.titanjr.dto.request.OfflineLoanApplyRequest;
import com.fangcang.titanjr.dto.request.RepaymentAmountComputeRequest;
import com.fangcang.titanjr.dto.request.RepaymentLoanRequest;
import com.fangcang.titanjr.dto.request.SaveLoanOrderInfoRequest;
import com.fangcang.titanjr.dto.request.SendMessageRequest;
import com.fangcang.titanjr.dto.request.SynLoanCreditOrderRequest;
import com.fangcang.titanjr.dto.request.SynLoanOrderRequest;
import com.fangcang.titanjr.dto.request.UploadApplyLoanFileToRsRequest;
import com.fangcang.titanjr.dto.request.UserInfoQueryRequest;
import com.fangcang.titanjr.dto.response.ApplyLoanResponse;
import com.fangcang.titanjr.dto.response.CancelLoanResponse;
import com.fangcang.titanjr.dto.response.ConfirmLoanOrderIsAvailableResponse;
import com.fangcang.titanjr.dto.response.FTPConfigResponse;
import com.fangcang.titanjr.dto.response.GetHistoryRepaymentListResponse;
import com.fangcang.titanjr.dto.response.GetLoanOrderInfoListResponse;
import com.fangcang.titanjr.dto.response.GetLoanOrderInfoResponse;
import com.fangcang.titanjr.dto.response.GetOrgLoanStatInfoResponse;
import com.fangcang.titanjr.dto.response.LoanOrderNotifyResponse;
import com.fangcang.titanjr.dto.response.OfflineLoanApplyResponse;
import com.fangcang.titanjr.dto.response.RepaymentAmountComputeResponse;
import com.fangcang.titanjr.dto.response.RepaymentLoanResponse;
import com.fangcang.titanjr.dto.response.SaveLoanOrderInfoResponse;
import com.fangcang.titanjr.dto.response.SynLoanOrderResponse;
import com.fangcang.titanjr.dto.response.UploadApplyLoanFileToRsResponse;
import com.fangcang.titanjr.dto.response.UserInfoPageResponse;
import com.fangcang.titanjr.entity.LoanApplyOrder;
import com.fangcang.titanjr.entity.LoanCreditOrder;
import com.fangcang.titanjr.entity.LoanExpiryStat;
import com.fangcang.titanjr.entity.LoanProductAmountStat;
import com.fangcang.titanjr.entity.LoanSevenDaysStat;
import com.fangcang.titanjr.entity.LoanSpecification;
import com.fangcang.titanjr.entity.TitanUser;
import com.fangcang.titanjr.entity.TitanVirtualOrg;
import com.fangcang.titanjr.entity.parameter.LoanQueryConditions;
import com.fangcang.titanjr.rs.dto.TBorrowRepayment;
import com.fangcang.titanjr.rs.dto.TUserArepayment;
import com.fangcang.titanjr.rs.manager.RSCreditManager;
import com.fangcang.titanjr.rs.manager.RSFileManager;
import com.fangcang.titanjr.rs.request.NewLoanApplyRequest;
import com.fangcang.titanjr.rs.request.OrderServiceAgreementConfirmRequest;
import com.fangcang.titanjr.rs.request.QueryBorrowinfoRequest;
import com.fangcang.titanjr.rs.request.QueryCreditMerchantInfoRequest;
import com.fangcang.titanjr.rs.request.QueryLoanApplyRequest;
import com.fangcang.titanjr.rs.request.QueryUserInitiativeRepaymentRequest;
import com.fangcang.titanjr.rs.request.RSFsFileUploadRequest;
import com.fangcang.titanjr.rs.request.StopLoanRequest;
import com.fangcang.titanjr.rs.request.UserInitiativeRepamentRequest;
import com.fangcang.titanjr.rs.response.OrderServiceAgreementConfirmResponse;
import com.fangcang.titanjr.rs.response.QueryBorrowinfoResponse;
import com.fangcang.titanjr.rs.response.QueryCreditMerchantInfoResponse;
import com.fangcang.titanjr.rs.response.QueryLoanApplyResponse;
import com.fangcang.titanjr.rs.response.QueryUserInitiativeRepaymentResponse;
import com.fangcang.titanjr.rs.response.RSFsFileUploadResponse;
import com.fangcang.titanjr.rs.response.StopLoanResponse;
import com.fangcang.titanjr.rs.response.UserInitiativeRepamentResponse;
import com.fangcang.titanjr.service.TitanFinancialLoanCreditService;
import com.fangcang.titanjr.service.TitanFinancialLoanService;
import com.fangcang.titanjr.service.TitanFinancialOrganService;
import com.fangcang.titanjr.service.TitanFinancialSendSMSService;
import com.fangcang.titanjr.service.TitanFinancialTradeService;
import com.fangcang.titanjr.service.TitanFinancialUserService;
import com.fangcang.titanjr.service.TitanOrderService;
import com.fangcang.titanjr.service.TitanSysconfigService;
import com.fangcang.titanjr.service.impl.loandispose.LoanProductDisposeAbstrator;
import com.fangcang.titanjr.service.impl.loandispose.LoanProductDisposeFactory;
import com.fangcang.titanjr.util.LoanTypeConvertUtil;
import com.fangcang.util.StringUtil;

@Service("titanFinancialLoanService")
public class TitanFinancialLoanServiceImpl implements TitanFinancialLoanService {

	private static final Log log = LogFactory
			.getLog(TitanFinancialLoanServiceImpl.class);

	@Resource
	private LoanOrderDao loanOrderDao;

	@Resource
	private LoanSpecificationDao loanSpecificationDao;

	@Resource
	private LoanCreditOrderDao loanCreditOrderDao;

	@Resource
	private RSCreditManager rsCreditManager;

	@Resource
	private TitanSysconfigService titanSysconfigService;

	@Resource
	private TitanFinancialOrganService organService;

	@Resource
	private RSFileManager rsFileManager;

	@Resource
	private PlatformOrderOprDao platformOrderOprDao;

	@Resource
	private TitanTransOrderDao titanTransOrderDao;

	@Resource
	private TitanFinancialLoanCreditService loanCreditService;

	@Resource
	private TitanFinancialUserService userService;

	@Resource
	private TitanOrderService orderService;

	@Resource
	private TitanFinancialTradeService tradeService;

	@Resource
	private TitanFinancialSendSMSService sendSMSService;

	@Resource
	private TitanVirtualOrgDao titanVirtualOrgDao;

	@Resource
	private LoanProductDisposeFactory loanProductDisposeFactory;
	
	
	@Resource
	private TitanFinancialTradeService titanFinancialTradeService;

	/**
	 * 贷款申请
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
	public ApplyLoanResponse applyLoan(ApplyLoanRequest req) throws Exception {
		ApplyLoanResponse response = new ApplyLoanResponse();

		if (req == null || req.getLcanSpec() == null
				|| req.getProductType() == null) {
			response.putErrorResult("参数错误");
			return response;
		}

		log.info("apply loan request = " + JsonConversionTool.toJson(req));

		// 检查用户的授信单是否正常
		LoanCreditOrder loanCreditOrder = new LoanCreditOrder();
		loanCreditOrder.setOrgCode(req.getOrgCode());
		List<LoanCreditOrder> loanCreditOrderList = loanCreditOrderDao
				.queryLoanCreditOrder(loanCreditOrder);
		try {

			if (loanCreditOrderList == null || loanCreditOrderList.isEmpty()) {
				log.error("credit order query fail!");
				throw new Exception("查询授信申请单失败");
			}
			loanCreditOrder = loanCreditOrderList.get(0);

			String loanCreditOrderNo = loanCreditOrder.getOrderNo();
			if (!StringUtil.isValidString(loanCreditOrderNo)) {
				log.error("授信申请单位空,机构编码OrgCode：" + req.getOrgCode());
				response.putErrorResult("授信申请单位空");
				return response;
			}

			log.info("credit orderNo = " + loanCreditOrderNo);

			QueryCreditMerchantInfoRequest qcrequest = new QueryCreditMerchantInfoRequest();
			qcrequest.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
			qcrequest
					.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
			qcrequest.setUserid(req.getOrgCode());
			qcrequest.setUserorderid(loanCreditOrderNo);
			QueryCreditMerchantInfoResponse qcResponse = rsCreditManager
					.queryCreditMerchantInfo(qcrequest);

			log.info("affirm credit orderNo whether available!");

			if (!qcResponse.isSuccess()
					|| !StringUtil.isValidString(qcResponse
							.getCreditavailability())) {
				log.error("授信申请查询失败,贷款申请单号loanCreditOrderNo："
						+ loanCreditOrderNo);
				response.putErrorResult("授信申请查询失败");
				return response;
			}

			LoanProductEnum productType = req.getProductType();

			boolean isEnough = new BigDecimal(
					qcResponse.getCreditavailability()).subtract(
					new BigDecimal(req.getLcanSpec().getAmount())).compareTo(
					BigDecimal.ZERO) == -1;
			if (isEnough) {
				log.error("授信额度不足，贷款申请单号loanCreditOrderNo：" + loanCreditOrderNo);
				response.putErrorResult("授信额度不足");
				return response;
			}

			String relateOrgCode = "";

			if (StringUtil.isValidString(req.getLcanSpec().getAccountName())) {
				TitanVirtualOrg org = new TitanVirtualOrg();
				org.setOrgName(req.getLcanSpec().getAccountName());
				List<TitanVirtualOrg> list = titanVirtualOrgDao
						.queryVirtualOrgInfos(org);
				if (list == null || list.isEmpty()) {
					response.putErrorResult("找不到收款机构！");
					return response;
				}
				relateOrgCode = list.get(0).getOrgCode();
			}
			// 保存相关数据
			boolean flag = this.saveLoanSpecBean(req.getLcanSpec());
			if (!flag) {
				log.error("保存包房贷订单失败");
				throw new Exception("保存包房贷订单失败");
			}

			// 保存贷款申请单
			flag = saveLoanOrderBean(req.getLcanSpec(), productType.getCode(),
					req.getOrgCode(), loanCreditOrderNo,
					Integer.valueOf(req.getOperator()));
			if (!flag) {
				throw new Exception("保存订单失败");
			}

//			if (!StringUtil.isValidString(req.getUrlKey())) {
//				log.error("贷款申请订单证明资料上传失败，orgcode:" + req.getOrgCode());
//				throw new Exception("保存订单失败");
//			}
			// 申请贷款
			NewLoanApplyRequest request = this.convertToNewLoanApplyRequest(
					req.getLcanSpec(), productType.getCode(), req.getOrgCode(),
					relateOrgCode);
			request.setUrlkey(req.getUrlKey());

			this.packLoanJSonData(request, req.getLcanSpec(),
					productType.getCode());

			LoanProductDisposeAbstrator disposeAbstrator = loanProductDisposeFactory
					.getDisposeAbstrator(productType);

			ApplyLoanResponse loanResponse = disposeAbstrator.applyLoan(
					request, req);

			// NewLoanApplyResponse loanResponse = rsCreditManager
			// .newLoanApply(request);
			if (!loanResponse.isResult()) {
				log.error(loanResponse.getReturnCode() + ":"
						+ loanResponse.getReturnMessage());
				throw new Exception(loanResponse.getReturnCode() + ":"
						+ loanResponse.getReturnMessage());
			}

			// 更新融数返回的贷款单号
			if(StringUtil.isValidString(loanResponse.getOrderNo())){
				LoanApplyOrder loanOrder = new LoanApplyOrder();
				loanOrder.setOrderid(loanResponse.getOrderNo());
				loanOrder.setOrderNo(request.getUserorderid());
				this.updateLoanOrderBean(loanOrder);
			}
			
			response.setOrderNo(request.getUserorderid());
			response.setOrderCreateTime(DateUtil.dateToString(new Date(),
					"yyyy-MM-dd HH:mm"));
			response.putSuccess();
			sendLoanSms(req.getLcanSpec().getOrderNo());
			return response;

		} catch (Exception e) {
			log.error("贷款申请异常,OrgCode:" + req.getOrgCode(), e);
			throw e;
		}
	}

	public UploadApplyLoanFileToRsResponse uploadApplyLoanFileToRs(
			UploadApplyLoanFileToRsRequest uploadApplyLoanFileToRsRequest) {
		UploadApplyLoanFileToRsResponse uploadApplyLoanFileToRsResponse = new UploadApplyLoanFileToRsResponse();
		String urlKey = null;
		try {
			urlKey = getApplyLoanUrlKey(
					uploadApplyLoanFileToRsRequest.getOrgCode(),
					uploadApplyLoanFileToRsRequest.getLoanApplyOrderNo(),
					uploadApplyLoanFileToRsRequest.getLoantype(),
					uploadApplyLoanFileToRsRequest.getContactNames());
		} catch (Exception e) {
			log.error("贷款合同附件上传失败，参数uploadApplyLoanFileToRsRequest："
					+ Tools.gsonToString(uploadApplyLoanFileToRsRequest));
			uploadApplyLoanFileToRsResponse.putErrorResult("贷款合同附件上传失败");
			return uploadApplyLoanFileToRsResponse;
		}
		if (StringUtil.isValidString(urlKey)) {
			uploadApplyLoanFileToRsResponse.setUrlKey(urlKey);
			uploadApplyLoanFileToRsResponse.putSuccess("贷款合同附件上传成功");
		}
		return uploadApplyLoanFileToRsResponse;
	}

	/**
	 * 上传包房贷文件
	 * 
	 * @param contactNames
	 *            文件名数组， 如：123.jpg,3333.jpg,4444.jpg
	 * @param loanApplyOrderNo
	 *            包房贷申请订单号
	 * @param Loantype
	 *            贷款类型：1-企业，2-个人
	 * @return
	 * @throws Exception
	 */
	private String getApplyLoanUrlKey(String orgCode, String loanApplyOrderNo,
			Integer Loantype, String contactNames) throws Exception {
		if (!StringUtil.isValidString(contactNames)) {
			throw new Exception("包房贷申请没有上传申请合同文件");
		}
		String orgLoanFileRootDir = TitanFinancialLoanServiceImpl.class
				.getClassLoader().getResource("").getPath()
				+ "tmp"
				+ FtpUtil.UPLOAD_PATH_LOAN_APPLY
				+ "/"
				+ orgCode
				+ "/"
				+ loanApplyOrderNo;
		String localFileDir;
		String localFather;
		if (Loantype == 1) {
			localFather = "EnterpriseLoanPackage";
			// 企业
			localFileDir = "/" + localFather + "/DocumentInfo";
		} else {
			localFather = "PersonalLoanPackage";
			// 个人
			localFileDir = "/" + localFather + "/PersonalDocumentInfo";
		}

		// 清理环境
		FileHelp.deleteFile(orgLoanFileRootDir);
		// 创建目录
		FtpUtil.makeLocalDirectory(orgLoanFileRootDir + localFileDir);
		// 下载
		try {
			FTPConfigResponse ftpConfigResponse = titanSysconfigService
					.getFTPConfig();
			FtpUtil ftpUtil = new FtpUtil(ftpConfigResponse.getFtpServerIp(),
					ftpConfigResponse.getFtpServerPort(),
					ftpConfigResponse.getFtpServerUser(),
					ftpConfigResponse.getFtpServerPassword());
			ftpUtil.ftpLogin();
			String[] fileNames = contactNames.split(",");
			for (String file : fileNames) {
				if (StringUtil.isValidString(file)) {
					ftpUtil.downloadFile(file, orgLoanFileRootDir
							+ localFileDir, FtpUtil.baseLocation
							+ FtpUtil.UPLOAD_PATH_LOAN_APPLY + "/" + orgCode
							+ "/" + loanApplyOrderNo);
				}
			}

			ftpUtil.ftpLogOut();
		} catch (Exception e) {
			log.error("下载ftp文件失败，路径：" + FtpUtil.baseLocation
					+ FtpUtil.UPLOAD_PATH_LOAN_APPLY + "/" + orgCode + "/"
					+ loanApplyOrderNo + "，文件为：" + contactNames, e);
			throw new Exception("文件下载失败", e);
		}

		File srcZipFile = FileHelp.zipFile(orgLoanFileRootDir + "/"
				+ localFather, localFather + "_src.zip");
		long zipFileLength = srcZipFile.length() / 1024;
		log.info("包房贷申请文件(" + srcZipFile.getName() + ")压缩后大小：" + zipFileLength
				+ " KB,包房订单号是[loanApplyOrderNo]:" + loanApplyOrderNo);
		// 小于10K，则表示文件不存在或者下载失败
		if (zipFileLength < 10) {
			log.error("从ftp下载文件时，文件太小或者不存在，原文件路径srcZipFile："
					+ srcZipFile.getAbsolutePath());
			throw new Exception("包房贷申请的文件下载失败或者文件太小");
		}
		// 加密
		String encryptFilePath = orgLoanFileRootDir + "/" + localFather
				+ ".zip";
		try {
			FileHelp.encryptRSFile(srcZipFile, encryptFilePath);
		} catch (Exception e) {
			log.error("encryptRSFile，融数授信申请资料文件加密失败，原文件路径srcZipFile："
					+ srcZipFile.getAbsolutePath(), e);
			throw new Exception("文件rsa加密失败", e);
		}

		// 上传到融数
		RSFsFileUploadRequest rsFsFileUploadRequest = new RSFsFileUploadRequest();
		rsFsFileUploadRequest.setUserid(orgCode);
		rsFsFileUploadRequest.setConstid(CommonConstant.RS_FANGCANG_CONST_ID);
		rsFsFileUploadRequest
				.setProductid(CommonConstant.RS_FANGCANG_PRODUCT_ID);
		rsFsFileUploadRequest
				.setType(FileTypeEnum.UPLOAD_FILE_73.getFileType());
		rsFsFileUploadRequest.setInvoiceDate(com.fangcang.util.DateUtil
				.getCurrentDate());
		rsFsFileUploadRequest.setPath(encryptFilePath);
		rsFsFileUploadRequest.setBacth(orgCode
				+ com.fangcang.util.DateUtil.getCurrentDate().getTime());

		RSFsFileUploadResponse rsFsFileUploadResponse = rsFileManager
				.fsFileUpload(rsFsFileUploadRequest);
		// 上传失败
		if (rsFsFileUploadResponse.isSuccess() == false
				|| (!StringUtil.isValidString(rsFsFileUploadResponse
						.getUrlKey()))) {
			log.error("包房贷单号loanApplyOrderNo:" + loanApplyOrderNo
					+ ",上传包房贷压缩包文件到融数失败,"
					+ Tools.gsonToString(rsFsFileUploadRequest));
			throw new Exception("文件上传失败,错误信息:"
					+ rsFsFileUploadResponse.getReturnMsg());
		}

		FileHelp.deleteFile(orgLoanFileRootDir);
		return rsFsFileUploadResponse.getUrlKey();
	}

	/**
	 * 保存贷款规格
	 * 
	 * @Title: saveLoanSpecBean
	 * @Description: TODO
	 * @param specInfo
	 * @return
	 * @throws Exception
	 * @return: boolean
	 */
	private boolean saveLoanSpecBean(LoanSpecBean specInfo) throws Exception {

		LoanSpecification specification = new LoanSpecification();
		try {
			// 针对包房贷的时候做一些规格处理
			if (specInfo instanceof LoanRoomPackSpecBean) {

				LoanRoomPackSpecBean loanSpecBean = (LoanRoomPackSpecBean) specInfo;
				specification.setAccount(loanSpecBean.getAccount());
				// specification.setTitanCode(loanSpecBean.getTitanCode());
				specification.setAccountName(loanSpecBean.getAccountName());
				specification.setBank(loanSpecBean.getBank());
				specification.setAccessory(loanSpecBean.getAccessory());
				Map<String, String> spec = new HashMap<String, String>();
				spec.put("beginDate",
						DateUtil.sdf.format(loanSpecBean.getBeginDate()));
				spec.put("endDate",
						DateUtil.sdf.format(loanSpecBean.getEndDate()));
				spec.put("hotelName", loanSpecBean.getHotleName());
				spec.put("roomNights", "" + loanSpecBean.getRoomNights());
				specification.setContent(JsonConversionTool.toJson(spec));
				specification.setOrderNo(loanSpecBean.getOrderNo());

			} else if (specInfo instanceof LoanSpecificationBean) {

				LoanSpecificationBean loanSpecBean = (LoanSpecificationBean) specInfo;
				specification.setAccount(loanSpecBean.getAccount());
				// specification.setTitanCode(loanSpecBean.getTitanCode());
				specification.setAccountName(loanSpecBean.getAccountName());
				specification.setBank(loanSpecBean.getBank());
				specification.setAccessory(loanSpecBean.getAccessory());
				specification.setContent(loanSpecBean.getContent());
				specification.setOrderNo(loanSpecBean.getOrderNo());
			}

			int row = loanSpecificationDao.saveLoanSpecification(specification);

			if (row > 0) {
				return true;
			}
			throw new Exception("保存包房单失效");
		} catch (Exception e) {
			log.error("保存包房贷信息失败", e);
			throw e;
		}
	}

	private void updateLoanOrderBean(LoanApplyOrder loanApplyOrder) {
		try {
			loanOrderDao.updateLoanApplyOrder(loanApplyOrder);
		} catch (Exception e) {
			log.error("保存融数返回贷款单号失败：" + loanApplyOrder.getOrderid() + ":" + e);
		}

	}

	private boolean saveLoanOrderBean(LoanSpecBean loanSpecBean, Integer type,
			String orgCode, String creditNo, int creatorId) throws Exception {
		LoanApplyOrder loanApplyOrder = new LoanApplyOrder();
		try {
			loanApplyOrder.setCreditOrderNo(creditNo);
			loanApplyOrder.setOrderNo(loanSpecBean.getOrderNo());
			
			if(StringUtil.isValidString(loanSpecBean.getAmount()))
			{
				loanApplyOrder.setAmount(Long.parseLong(loanSpecBean.getAmount()));
			}
			loanApplyOrder.setCreatorId(creatorId);
			loanApplyOrder.setCreateTime(new Date());
			loanApplyOrder.setOrgCode(orgCode);
			loanApplyOrder
					.setProductId(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
			loanApplyOrder.setRateTmp(CommonConstant.RATE_TEMPLETE);
			loanApplyOrder.setActualAmount(0L);
			loanApplyOrder.setProductType(type);
			loanApplyOrder.setRsorgId(orgCode);
			loanApplyOrder.setRepaymentType(1);// 1 按日计利，随借随还
			loanApplyOrder
					.setRspId(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
			loanApplyOrder
					.setStatus(LoanApplyOrderEnum.LoanOrderStatus.LOAN_APPLYING.status);
			int row = loanOrderDao.saveLoanApplyOrder(loanApplyOrder);
			if (row > 0) {
				return true;
			}
			throw new Exception("查询授信申请单失败");
		} catch (Exception e) {
			log.error("保存包房贷订单失效");
			throw e;
		}
	}

	/**
	 * 
	 * @param loanSpecBean
	 * @param type
	 * @param orgCode
	 * @param relateOrgCode
	 *            贷款接收方机构编码
	 * @return
	 * @throws Exception
	 */
	private NewLoanApplyRequest convertToNewLoanApplyRequest(
			LoanSpecBean loanSpecBean, Integer type, String orgCode,
			String relateOrgCode) throws Exception {
		try {
			OrgDTO param = new OrgDTO();
			param.setOrgcode(orgCode);
			param = organService.queryOrg(param);
			NewLoanApplyRequest request = new NewLoanApplyRequest();
			request.setAmount(loanSpecBean.getAmount());
			request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
			request.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
			request.setRatetemplate(CommonConstant.RATE_TEMPLETE);
			request.setReqesttime("");
			request.setRequestdate(DateUtil.sdf4.format(new Date()));
			request.setUserid(orgCode);
			request.setUsername(param.getOrgname());
			request.setUserorderid(loanSpecBean.getOrderNo());
			request.setUserrelateid(relateOrgCode);
			return request;
		} catch (Exception e) {
			log.error("申请贷款失败");
			throw e;
		}
	}

	private void packLoanJSonData(NewLoanApplyRequest request,
			LoanSpecBean loanSpecBean, Integer type) {
		NewLoanApplyJsonDataBean moneyCreditJsonData = new NewLoanApplyJsonDataBean();
//		if (LoanProductEnum.ROOM_PACK.getCode() == type.intValue()) {
//			loanRoomPackSpecBean = (LoanRoomPackSpecBean) loanSpecBean;
//		}
		moneyCreditJsonData.setLoanApplicateName(request.getUsername());
		moneyCreditJsonData.setInParty(loanSpecBean
				.getAccountName());
		moneyCreditJsonData.setUserOrderId(request.getUserorderid());
		moneyCreditJsonData.setOrderTime(request.getReqesttime());
		
		moneyCreditJsonData.setInBankAccount(loanSpecBean.getBank());
		moneyCreditJsonData.setInBankAccountNo(loanSpecBean
				.getAccount());
		moneyCreditJsonData.setDeliveryStatus("");
		moneyCreditJsonData.setReceivingState("");
		moneyCreditJsonData.setReceiptAddress("深圳市宝安区");
		moneyCreditJsonData.setCode("10000000000009");
		moneyCreditJsonData.setUnitPrice("");
		moneyCreditJsonData.setOrderAmount(loanSpecBean.getAmount());
		moneyCreditJsonData.setShenqingAmount(loanSpecBean.getAmount());
		
		if (LoanProductEnum.ROOM_PACK.getCode() == type.intValue()) {
			LoanRoomPackSpecBean loanRoomPackSpecBean  = (LoanRoomPackSpecBean)loanSpecBean;
			moneyCreditJsonData.setProductName(LoanProductEnum.ROOM_PACK.getDesc());
			moneyCreditJsonData.setNumber(loanRoomPackSpecBean.getRoomNights() + "");
		}else if(LoanProductEnum.OPERACTION.getCode() == type.intValue())
		{
			moneyCreditJsonData.setProductName(LoanProductEnum.OPERACTION.getDesc());
		}
		moneyCreditJsonData.setOrderType("");
		moneyCreditJsonData.setRootInstCd(request.getRootinstcd());
		moneyCreditJsonData.setLoanTerm("90");
		JSON result = JSONSerializer.toJSON(moneyCreditJsonData);
		request.setJsondata(result.toString());

	}

	@Override
	public CancelLoanResponse cancelLoan(CancelLoanRequest req) {

		CancelLoanResponse loanResponse = new CancelLoanResponse();

		if (req == null || !StringUtil.isValidString(req.getOrderNo())
				|| !StringUtil.isValidString(req.getOrgCode())) {
			log.error("request param is null [ orderNo = " + req.getOrderNo()
					+ "  orgCode=" + req.getOrgCode() + "]");
			loanResponse.putParamError();
			return loanResponse;
		}

		StopLoanRequest request = new StopLoanRequest();
		request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		request.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		request.setUserorderid(req.getOrderNo());
		request.setUserid(req.getOrgCode());
		request.setOper(2);

		StopLoanResponse response = rsCreditManager.stopLoan(request);

		log.info("stop loan result " + JsonConversionTool.toJson(response));

		if (response != null && response.isSuccess()) {
			LoanApplyOrder applyOrder = getBaseLoanApplyInfo(req.getOrderNo(),
					req.getOrgCode());
			applyOrder.setStatus(LoanOrderStatusEnum.LOAN_UNDO.getKey());
			loanOrderDao.updateLoanApplyOrder(applyOrder);

			loanResponse.putSuccess();

			return loanResponse;
		}

		loanResponse.putErrorResult("stop loan fail!");

		return loanResponse;
	}

	private LoanApplyOrder getBaseLoanApplyInfo(String orderNo, String orgCode) {

		LoanQueryConditions conditions = new LoanQueryConditions();
		conditions.setOrderNo(orderNo);
		conditions.setOrgCode(orgCode);

		PaginationSupport<LoanApplyOrder> paginationSupport = new PaginationSupport<LoanApplyOrder>();
		paginationSupport = loanOrderDao.queryLoanApplyOrder(conditions,
				paginationSupport);

		for (LoanApplyOrder tempApplyOrder : paginationSupport.getItemList()) {
			return tempApplyOrder;
		}

		return null;
	}

	/**
	 * 同步贷款单信息
	 * 
	 * @param req
	 * @return
	 */
	public SynLoanOrderResponse synLoanOrderInfo(SynLoanOrderRequest req) {

		SynLoanOrderResponse orderResponse = new SynLoanOrderResponse();

		if (req == null || !StringUtil.isValidString(req.getOrderNo())
				|| !StringUtil.isValidString(req.getOrgCode())) {
			log.error("request param is null [ orderNo = " + req.getOrderNo()
					+ "  orgCode=" + req.getOrgCode() + "]");
			orderResponse.putParamError();
			return orderResponse;
		}

		String orderNo = req.getOrderNo();
		String orgCode = req.getOrgCode();

		LoanApplyOrder loanApplyOrder = getBaseLoanApplyInfo(orderNo, orgCode);

		if (loanApplyOrder == null) {
			log.error("query loan apply order is null  [ orderNo = " + orgCode
					+ "  orgCode=" + orderNo + "]");
			orderResponse.putParamError();
			return orderResponse;
		}
		// 查询贷款单信息
		QueryLoanApplyRequest request = new QueryLoanApplyRequest();
		request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		request.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		request.setUserorderid(orderNo);
		request.setUserid(orgCode);

		QueryLoanApplyResponse rsp = rsCreditManager.queryLoanApply(request);

		if (!rsp.isSuccess()) {

			log.error("query Loan Apply info fail.  [ orderNo = " + orderNo
					+ "  orgCode=" + orgCode + "]");
		}

		log.info("orderNo:"+orderNo+",query loan apply result " + JsonConversionTool.toJson(rsp));
		// 查询贷款应还信息
		QueryBorrowinfoRequest bReq = new QueryBorrowinfoRequest();
		bReq.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		bReq.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		bReq.setUserid(orgCode);
		bReq.setUserorderid(orderNo);

		QueryBorrowinfoResponse brsp = rsCreditManager.queryBorrowinfo(bReq);


		log.info("orderNo:"+orderNo+", query borrow info result " + JsonConversionTool.toJson(brsp));

		// 查询主动还款信息 
		QueryUserInitiativeRepaymentRequest ureq = new QueryUserInitiativeRepaymentRequest();
		ureq.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		ureq.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		ureq.setUserid(orgCode);
		ureq.setUserorderid(orderNo);
		QueryUserInitiativeRepaymentResponse response = rsCreditManager
				.queryUserInitiativeRepayment(ureq);

		 

		log.info("orderNo:"+orderNo+",query User Initiative result "
				+ JsonConversionTool.toJson(response));

		TBorrowRepayment borrowRepayment = brsp.gettBorrowRepayment();

		// 计算应还信息
		if (borrowRepayment != null) {
			BigDecimal interestAmount = new BigDecimal(
					borrowRepayment.getUsershouldinterest());
			interestAmount = interestAmount.add(new BigDecimal(borrowRepayment
					.getUseroverduefine()).add(new BigDecimal(borrowRepayment
					.getUseroverdueinterest())));

			// 设置应还的金额及利息
			loanApplyOrder.setShouldCapital(Long.valueOf(borrowRepayment
					.getUsershouldcapital()));
			loanApplyOrder.setShouldInterest(interestAmount.longValue());

			// 设置用户还款到期日
			if (StringUtil.isValidString(borrowRepayment
					.getUsershouldrepaymentdate())) {
				try {
					loanApplyOrder
							.setActualRepaymentDate(DateUtil.sdf4
									.parse(borrowRepayment
											.getUsershouldrepaymentdate()));
				} catch (ParseException e) {
					log.error("", e);
				}
			}
		}

		// 保存最后一次主动还款记录，用户后面对还款状态进行判断是否贷款已经结清
		TUserArepayment lastUserArepayment = null;

		// 设置已还款信息
		if (response.gettUserArepaymentList() != null
				&& response.gettUserArepaymentList().size() > 0) {

			BigDecimal arepayAmount = new BigDecimal(0);
			BigDecimal arepayInterest = new BigDecimal(0);

			String lastRepaymentDate = null;

			for (TUserArepayment arepayment : response.gettUserArepaymentList()) {
				// 识别出有用的还款记录，1 标示全部还款 4标示部分还款
				if ("1".equals(arepayment.getStatusid())
						|| "4".equals(arepayment.getStatusid())) {

					arepayAmount = arepayAmount.add(new BigDecimal(arepayment
							.getActivecapital()));

					arepayInterest = arepayInterest.add(new BigDecimal(
							arepayment.getActiveinterest()));

					arepayInterest = arepayInterest.add(new BigDecimal(
							arepayment.getActiveoverduefine())
							.add(new BigDecimal(arepayment
									.getActiveoverdueinterest())));

					lastRepaymentDate = arepayment.getActiverepaymentdate();
					lastUserArepayment = arepayment;
				}
			}

			// 设置用户最后一次还款的日期
			if (StringUtil.isValidString(lastRepaymentDate)) {
				try {
					loanApplyOrder.setLastRepaymentDate(DateUtil.sdf4
							.parse(lastRepaymentDate));
				} catch (ParseException e) {
					log.error("", e);
				}
			}
			// 设置已还的金额及利息
			loanApplyOrder.setRepaymentPrincipal(arepayAmount.longValue());
			loanApplyOrder.setRepaymentInterest(arepayInterest.longValue());
		}

		if (StringUtil.isValidString(rsp.getLoanmoney())) {
			loanApplyOrder.setActualAmount(Long.parseLong(rsp.getLoanmoney()));
		}
		// 将融数平台的状态映射成房仓的状态
		LoanOrderStatusEnum orderStatusEnum = LoanTypeConvertUtil
				.rsStatusMap(rsp.getStatustring());
		// // 转账
		// if (orderStatusEnum.getKey() ==
		// LoanOrderStatusEnum.HAVE_LOAN.getKey()
		// && (LoanOrderStatusEnum.AUDIT_PASS.getKey() == loanApplyOrder
		// .getStatus()
		// || LoanOrderStatusEnum.LOAN_REQ_ING.getKey() == loanApplyOrder
		// .getStatus() || LoanOrderStatusEnum.WAIT_AUDIT
		// .getKey() == loanApplyOrder.getStatus())) {
		// // 放款成功后，下单，转账逻辑
		// if (loanApplyOrder.getActualAmount() != null
		// && loanApplyOrder.getActualAmount() > 0) {
		// try {
		// log.info("贷款通知时转账流程->状态达到转账要求，贷款订单号loanOrderNo：" + orderNo
		// + ",orgCode:" + orgCode);
		// loanPay(orgCode, orderNo, loanApplyOrder.getActualAmount()
		// .toString());
		// } catch (Exception e) {
		// log.error("贷款转账失败，付款方机构id:" + orgCode + ",贷款申请订单号orderNo："
		// + orderNo, e);
		// }
		// } else {
		// log.info("贷款通知时转账流程->放款金额不符合转账要求(没有大于0)，贷款订单号loanOrderNo："
		// + orderNo);
		// }
		// } else {
		// log.info("贷款通知时转账流程->贷款订单状态不符合转账要求，贷款订单号loanOrderNo：" + orderNo
		// + ",状态:" + orderStatusEnum.getDesc());
		// }
		// 融数方是否是终审通过，如果是终审通过那么我方如果是待审核，那么就需要确认一次授信协议哦
		if (orderStatusEnum != null
				&& (LoanOrderStatusEnum.AUDIT_PASS.getKey() == orderStatusEnum
						.getKey() || LoanOrderStatusEnum.LENDING_ING.getKey() == orderStatusEnum
						.getKey())) {

			if (LoanOrderStatusEnum.LOAN_REQ_ING.getKey() == loanApplyOrder
					.getStatus()
					|| LoanOrderStatusEnum.WAIT_AUDIT.getKey() == loanApplyOrder
							.getStatus()) {

				log.info("orderNo:"+orderNo+", confirm loan order protocol ");
				// 进行协议确认
				OrderServiceAgreementConfirmRequest cReq = new OrderServiceAgreementConfirmRequest();

				cReq.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
				cReq.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
				cReq.setUserid(orgCode);
				cReq.setUserorderid(orderNo);
				cReq.setUserflag("2");
				cReq.setMerchanturlkey("xxxxx");

				OrderServiceAgreementConfirmResponse confirmResponse = rsCreditManager
						.agreementConfirm(cReq);

				if (confirmResponse == null || !confirmResponse.isSuccess()) {
					log.error("orderNo:"+orderNo+",confirm loan order protocol fail!  "
							+ JsonConversionTool.toJson(confirmResponse));
				}
			}
		}
		if (orderStatusEnum != null) {
			loanApplyOrder.setStatus(orderStatusEnum.getKey());
		}

		// 表示逾期
		if (borrowRepayment != null
				&& "1".equals(borrowRepayment.getUseroverdueflag())) {
			loanApplyOrder.setStatus(LoanOrderStatusEnum.LOAN_EXPIRY.getKey());
		}

		// 如果没生成还款计划，需要设置默认值
		if (borrowRepayment == null) {
			// 还款计划没生成，则设置贷款金额为应还金额
			if (loanApplyOrder.getShouldCapital() == null) {
				if(StringUtil.isValidString(rsp
						.getLoanmoney()))
				{
					loanApplyOrder.setShouldCapital(Long.parseLong(rsp
						.getLoanmoney()));
				}
			}
			// 如果没生成还款计划的，那么还款日期先采用+90天方式来算
			if (loanApplyOrder.getActualRepaymentDate() == null &&  rsp.getLoandate() != null) {
				Date rDate = DateUtils.addDays(
						DateUtil.toDataYYYYMMDD(rsp.getLoandate()),
						(CommonConstant.RS_LOAN_REPAYMENT_TIME - 1));
				loanApplyOrder.setActualRepaymentDate(rDate);//
			}

			// 最后一次还款历史不为空，并且1标示用户已经全部还款，那么订单就结束
			if (lastUserArepayment != null
					&& "1".equals(lastUserArepayment.getStatusid())) {
				// 恭喜已经还清贷款了哈
				loanApplyOrder.setStatus(LoanOrderStatusEnum.LOAN_FINISH
						.getKey());
			}
		}

		// 放款日期以融数的为准哦
		if (StringUtil.isValidString(rsp.getLoandate())) {
			try {
				loanApplyOrder.setRelMoneyTime(DateUtil.sdf4.parse(rsp
						.getLoandate()));
			} catch (ParseException e) {
				log.error("", e);
			}
		}

		// 如果申请贷款的金额为空，那么直接把贷款金额直接设置为用户申请贷款的金额（线下贷款单同步场景）
		if (loanApplyOrder.getAmount() == null
				|| loanApplyOrder.getAmount() <= 0l) {
			loanApplyOrder.setAmount(Long.parseLong(rsp.getLoanmoney()));
		}

		loanOrderDao.updateLoanApplyOrder(loanApplyOrder);
		orderResponse.putSuccess();

		return orderResponse;
	}

	/**
	 * 发送贷款通知短信
	 * 
	 * @param orderNo
	 *            贷款订单号
	 */
	private void sendLoanSms(String orderNo) {
		LoanQueryConditions loanQueryConditions = new LoanQueryConditions();
		loanQueryConditions.setOrderNo(orderNo);
		List<LoanApplyOrder> loanApplyOrderList = loanOrderDao
				.listLoanApplyOrder(loanQueryConditions);
		if (CollectionUtils.isNotEmpty(loanApplyOrderList)) {
			LoanApplyOrder loanApplyOrder = loanApplyOrderList.get(0);
			TitanUser titanUser = null;
			// 申请人
			if (loanApplyOrder.getCreatorId() != null
					&& loanApplyOrder.getCreatorId() > 0) {
				UserInfoQueryRequest userInfoQueryRequest = new UserInfoQueryRequest();
				userInfoQueryRequest
						.setTfsUserId(loanApplyOrder.getCreatorId());
				UserInfoPageResponse userInfoPageResponse = userService
						.queryUserInfoPage(userInfoQueryRequest);
				titanUser = userInfoPageResponse
						.getTitanUserPaginationSupport().getItemList().get(0);
			}

			LoanSpecification loanSpecificationParam = new LoanSpecification();
			loanSpecificationParam.setOrderNo(orderNo);
			List<LoanSpecification> loanSpecificationList = loanSpecificationDao
					.queryLoanSpecification(loanSpecificationParam);
			LoanSpecification loanSpecification = loanSpecificationList.get(0);
			try {
				boolean isExistsCreator = false;// 贷款单是否存在创建人
				// 发给申请人
				if (titanUser != null) {
					sendLoanSms(titanUser.getUserloginname(),
							titanUser.getUsername(), orderNo, loanApplyOrder
									.getAmount().toString(),
							loanSpecification.getAccountName(),
							loanApplyOrder.getStatus());
					isExistsCreator = true;
				}
				// 发给管理员
				if (isExistsCreator == false) {
					OrgDTO orgDTO = new OrgDTO();
					orgDTO.setUserid(loanApplyOrder.getOrgCode());
					orgDTO = organService.queryOrg(orgDTO);
					String receiveAddress;
					String username;
					if(orgDTO.getUsertype().equals(TitanOrgEnum.UserType.ENTERPRISE.getKey())){//企业用户
						//给联系人发
						receiveAddress = orgDTO.getMobiletel();
						username = orgDTO.getConnect();
					}else{
						//给管理员发
						UserInfoQueryRequest userInfoQueryRequest = new UserInfoQueryRequest();
						userInfoQueryRequest.setIsadmin(1);
						userInfoQueryRequest.setOrgCode(orgDTO.getOrgcode());
						UserInfoPageResponse userInfoPageResponse = userService.queryUserInfoPage(userInfoQueryRequest);
						receiveAddress = userInfoPageResponse.getTitanUserPaginationSupport().getItemList().get(0).getUserloginname();
						username = userInfoPageResponse.getTitanUserPaginationSupport().getItemList().get(0).getUsername();
					}
					
					sendLoanSms(receiveAddress,
							username, orderNo,
							loanApplyOrder.getAmount().toString(),
							loanSpecification.getAccountName(),
							loanApplyOrder.getStatus());
					
				}
			} catch (Exception e) {
				log.error("贷款通知短信或者邮件发送失败,订单号orderNo：" + orderNo, e);
			}
		}
	}

	/**
	 * 发送贷款通知短信
	 * 
	 * @param receiveAddress
	 *            信息接收者(手机号码或者邮箱)
	 * @param creatorName
	 *            申请人
	 * @param createTime
	 *            贷款时间
	 * @param orderNo
	 *            贷款订单号
	 * @param loanAmount
	 *            贷款金额(单位：分)
	 * @param loanReceive
	 *            贷款收款方
	 * @param loanOrderStatus
	 *            贷款订单状态
	 */
	private void sendLoanSms(String receiveAddress, String creatorName,
			String orderNo, String loanAmount, String loanReceive,
			int loanOrderStatus) {

		String subject = "";
		String content = "";
		loanAmount = NumberUtil.covertToYuan(loanAmount);
		if (loanOrderStatus == LoanOrderStatusEnum.LOAN_REQ_ING.getKey()) {// 申请提交成功
			Object[] subjectParam = new Object[] {};
			subject = MessageFormat.format(
					SMSTemplate.LOAN_REQ_ING.getSubject(), subjectParam);
			Object[] contentParam = new Object[] { creatorName, orderNo,
					loanAmount, loanReceive };
			content = MessageFormat.format(
					SMSTemplate.LOAN_REQ_ING.getContent(), contentParam);
		} else if (loanOrderStatus == LoanOrderStatusEnum.AUDIT_PASS.getKey()) {// 审核通过
			Object[] subjectParam = new Object[] {};
			subject = MessageFormat.format(
					SMSTemplate.LOAN_AUDIT_PASS.getSubject(), subjectParam);
			Object[] contentParam = new Object[] { creatorName, orderNo,
					loanAmount, loanReceive };
			content = MessageFormat.format(
					SMSTemplate.LOAN_AUDIT_PASS.getContent(), contentParam);
		} else if (loanOrderStatus == LoanOrderStatusEnum.AUDIT_FIAL.getKey()) {// 审核不通过
			Object[] subjectParam = new Object[] {};
			subject = MessageFormat.format(
					SMSTemplate.LOAN_AUDIT_FIAL.getSubject(), subjectParam);
			Object[] contentParam = new Object[] { creatorName, orderNo,
					loanAmount, loanReceive };
			content = MessageFormat.format(
					SMSTemplate.LOAN_AUDIT_FIAL.getContent(), contentParam);
		} else if (loanOrderStatus == LoanOrderStatusEnum.HAVE_LOAN.getKey()) {// 已放款
			Object[] subjectParam = new Object[] {};
			subject = MessageFormat.format(
					SMSTemplate.LOAN_HAVE_LOAN.getSubject(), subjectParam);
			Object[] contentParam = new Object[] { creatorName, orderNo,
					loanAmount, loanReceive };
			content = MessageFormat.format(
					SMSTemplate.LOAN_HAVE_LOAN.getContent(), contentParam);
		} else if (loanOrderStatus == LoanOrderStatusEnum.LENDING_FAIL.getKey()) {// 放款失败
			Object[] subjectParam = new Object[] {};
			subject = MessageFormat.format(
					SMSTemplate.LOAN_LENDING_FAIL.getSubject(), subjectParam);
			Object[] contentParam = new Object[] { creatorName, orderNo,
					loanAmount, loanReceive };
			content = MessageFormat.format(
					SMSTemplate.LOAN_LENDING_FAIL.getContent(), contentParam);
		}

		SendMessageRequest sendMessageRequest = new SendMessageRequest();
		sendMessageRequest.setReceiveAddress(receiveAddress);
		sendMessageRequest
				.setMerchantCode(CommonConstant.FANGCANG_MERCHANTCODE);
		sendMessageRequest.setContent(content);
		sendMessageRequest.setSubject(subject);
    	try {
    		sendSMSService.asynSendMessage(sendMessageRequest);
		} catch (Exception e) {
			log.error("贷款通知短信或者邮件发送失败,内容content："+content+",接收者receiveAddress:"+receiveAddress+",订单号orderNo："+orderNo, e);
		}

	}


	// /***
	// * 贷款转账
	// *
	// * @param payOrgCode
	// * 付款机构
	// * @param loanOrderNo
	// * 贷款申请订单号
	// * @param loanActualAmount
	// * 实际转账金额
	// * @return
	// */
	// private boolean loanPay(String payOrgCode, String loanOrderNo,
	// String loanActualAmount) {
	// boolean payState = false;
	// // 是否已经转账成功，不能重复转账
	// TransOrderRequest transOrderRequest = new TransOrderRequest();
	// transOrderRequest.setPayorderno(loanOrderNo);
	// TransOrderDTO transOrderDTO = orderService
	// .queryTransOrderDTO(transOrderRequest);
	// if (transOrderDTO == null
	// || (!OrderStatusEnum.ORDER_SUCCESS.getStatus().equals(
	// transOrderDTO.getStatusid()))) {// 转账不成功，继续转账
	// log.info("贷款通知时转账流程->未转账或者转账不成功，继续转账，贷款订单号loanOrderNo："
	// + loanOrderNo + ",付款方OrgCode:" + payOrgCode);
	// LoanSpecification loanSpecification = new LoanSpecification();
	// loanSpecification.setOrderNo(loanOrderNo);
	// List<LoanSpecification> loanSpecificationList = loanSpecificationDao
	// .queryLoanSpecification(loanSpecification);
	// if (CollectionUtils.isNotEmpty(loanSpecificationList)) {
	//
	// String titanCode = loanSpecificationList.get(0).getTitanCode();
	// OrgDTO relateOrgDTO = new OrgDTO();
	// relateOrgDTO.setTitancode(titanCode);
	// relateOrgDTO = organService.queryOrg(relateOrgDTO);
	//
	// String orderId = OrderGenerateService.genLocalOrderNo();
	// String requestno = OrderGenerateService.genResquestNo();
	// // 下单
	// TitanOrderRequest titanOrderRequest = new TitanOrderRequest();
	// titanOrderRequest.setAmount(NumberUtil
	// .covertToYuan(loanActualAmount));
	// titanOrderRequest.setCurrencyType("1");// 人民币
	// titanOrderRequest.setGoodsDetail("贷款付款转账");
	// titanOrderRequest.setGoodsId(loanOrderNo);
	// titanOrderRequest.setGoodsName("贷款");
	// titanOrderRequest.setPayerType(PayerTypeEnum.LOAN.getKey());
	// titanOrderRequest
	// .setProductId(CommonConstant.RS_FANGCANG_PRODUCT_ID_230);
	// titanOrderRequest.setUserId(payOrgCode);// 付款方
	// titanOrderRequest.setRuserId(relateOrgDTO.getOrgcode());// 接收方
	// TransOrderCreateResponse transOrderCreateResponse = tradeService
	// .saveTitanTransOrder(titanOrderRequest);
	// if (transOrderCreateResponse.isResult()) {
	// TitanTransOrder transOrderParam = new TitanTransOrder();
	// transOrderParam.setTransid(transOrderCreateResponse
	// .getTransId());
	// transOrderParam.setOrderid(orderId);
	// titanTransOrderDao
	// .updateTitanTransOrderByTransId(transOrderParam);
	// // 转账
	// TransferRequest transferRequest = new TransferRequest();
	// transferRequest
	// .setMerchantcode(CommonConstant.RS_FANGCANG_CONST_ID);
	// transferRequest
	// .setIntermerchantcode(CommonConstant.RS_FANGCANG_CONST_ID);
	// transferRequest
	// .setConditioncode(ConditioncodeEnum.ADD_OEDER);
	// transferRequest.setUserid(payOrgCode);
	// transferRequest.setUserrelateid(relateOrgDTO.getOrgcode());
	// transferRequest.setAmount(loanActualAmount);
	// transferRequest
	// .setProductId(CommonConstant.RS_FANGCANG_PRODUCT_ID_230);
	// transferRequest
	// .setInterproductid(CommonConstant.RS_FANGCANG_PRODUCT_ID);
	// transferRequest.setOrderid(orderId);
	// transferRequest.setRequestno(requestno);
	// transferRequest.setRequesttime(DateUtil.sdf4
	// .format(new Date()));
	// transferRequest
	// .setTransfertype(TransfertypeEnum.BRANCH_TRANSFER);
	// transferRequest.setUserfee("0");
	// TransferResponse transferResponse = new TransferResponse();
	// try {
	// transferResponse = tradeService
	// .transferAccounts(transferRequest);
	// if (transferResponse.isResult()) {
	// TransOrderDTO orderStatusidParam = new TransOrderDTO();
	// orderStatusidParam
	// .setStatusid(OrderStatusEnum.ORDER_SUCCESS
	// .getStatus());
	// orderStatusidParam
	// .setTransid(transOrderCreateResponse
	// .getTransId());
	// orderService.updateTransOrder(orderStatusidParam);
	// log.info("贷款通知时转账流程->转账成功，贷款订单号loanOrderNo："
	// + loanOrderNo + ",requestno:" + requestno);
	// payState = true;
	// } else {
	// log.error("贷款转账失败,原因："
	// + transferResponse.getReturnMessage()
	// + ",贷款订单号loanOrderNo：" + loanOrderNo
	// + ",转账参数："
	// + Tools.gsonToString(transferRequest));
	// }
	// } catch (Exception e) {
	// log.error(
	// "贷款转账失败loanPay，付款方机构id:" + payOrgCode
	// + ",转账参数："
	// + Tools.gsonToString(transferRequest),
	// e);
	// }
	// }
	// }
	// } else {
	// log.info("贷款通知时转账流程->已经转账，本次通知不需要转账，贷款订单号loanOrderNo："
	// + loanOrderNo);
	// }
	// return payState;
	// }

	/**
	 * 對還款的金額進行預分析
	 */
	public RepaymentAmountComputeResponse repaymentAmountCompute(
			RepaymentAmountComputeRequest req) {

		RepaymentAmountComputeResponse loanResponse = new RepaymentAmountComputeResponse();

		if (req == null || !StringUtil.isValidString(req.getOrderNo())
				|| !StringUtil.isValidString(req.getOrgCode())) {
			log.error("request param is null [ orderNo = " + req.getOrderNo()
					+ "  orgCode=" + req.getOrgCode() + "]");
			loanResponse.putParamError();
			return loanResponse;
		}

		LoanApplyOrder loanApplyOrder = getBaseLoanApplyInfo(req.getOrderNo(),
				req.getOrgCode());

		if (loanApplyOrder == null) {
			log.error("query loan apply order is null  [ orderNo = "
					+ req.getOrderNo() + "  orgCode=" + req.getOrgCode() + "]");
			loanResponse.putSysError();
			return loanResponse;
		}

		QueryBorrowinfoRequest bReq = new QueryBorrowinfoRequest();
		bReq.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		bReq.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		bReq.setUserid(req.getOrgCode());
		bReq.setUserorderid(req.getOrderNo());

		QueryBorrowinfoResponse brsp = rsCreditManager.queryBorrowinfo(bReq);

		if (!brsp.isSuccess() || brsp.gettBorrowRepayment() == null) {

			log.error("query borrow info fail.  [ orderNo = "
					+ req.getOrderNo() + "  orgCode=" + req.getOrgCode() + "]");
			loanResponse.putSysError();
			return loanResponse;
		}

		BigDecimal amount = new BigDecimal(req.getAmount());

		TBorrowRepayment repayment = brsp.gettBorrowRepayment();

		BigDecimal realoverduecapitalAmount = amountCompute(amount,
				new BigDecimal(repayment.getUseroverduefine()));
		amount = amount.subtract(realoverduecapitalAmount);
		loanResponse.setRealoverduecapitalAmount(realoverduecapitalAmount
				.longValue());// 逾期罚金还款金额

		BigDecimal realoverdueinterestAmount = amountCompute(amount,
				new BigDecimal(repayment.getUseroverdueinterest()));
		amount = amount.subtract(realoverdueinterestAmount);
		loanResponse.setRealoverdueinterestAmount(realoverdueinterestAmount
				.longValue());// 逾期利息还款金额

		BigDecimal usershouldinterest = amountCompute(amount, new BigDecimal(
				repayment.getUsershouldinterest()));
		amount = amount.subtract(usershouldinterest);
		loanResponse.setRealinterestAmount(usershouldinterest.longValue());// 利息还款金额

		BigDecimal usershouldcapital = amountCompute(amount, new BigDecimal(
				repayment.getUsershouldcapital()));
		amount = amount.subtract(usershouldcapital);
		loanResponse.setRealcapitalAmount(usershouldcapital.longValue());// 本金

		loanResponse.setUseroverduefine(repayment.getUseroverduefine());
		loanResponse.setUseroverdueinterest(repayment.getUseroverdueinterest());
		loanResponse.setUsershouldcapital(repayment.getUsershouldcapital());
		loanResponse.setUsershouldinterest(repayment.getUsershouldinterest());

		loanResponse.putSuccess();

		return loanResponse;
	}

	/**
	 * 金额计算方法
	 * 
	 * @param amount
	 * @param bAmount
	 * @return
	 */
	private BigDecimal amountCompute(BigDecimal amount, BigDecimal bAmount) {

		if (bAmount.longValue() <= 0 || amount.longValue() <= 0) {
			return new BigDecimal(0);
		}

		// 为分配完毕则继续减去
		BigDecimal tempBigDecimal = amount.subtract(bAmount);

		// 如果减去后小于等于0，那么标示剩余的金额分配了
		if (tempBigDecimal.longValue() <= 0) {
			return amount;
		}
		return bAmount;
	}

	private String convetRepaymentAmount(long amount) {
		if (amount <= 0) {
			return null;
		}
		return String.valueOf(amount);
	}

	@Override
	public RepaymentLoanResponse repaymentLoan(RepaymentLoanRequest req) {

		RepaymentLoanResponse loanResponse = new RepaymentLoanResponse();

		if (req == null || !StringUtil.isValidString(req.getOrderNo())
				|| !StringUtil.isValidString(req.getOrgCode())) {
			log.error("request param is null [ orderNo = " + req.getOrderNo()
					+ "  orgCode=" + req.getOrgCode() + "]");
			loanResponse.putParamError();
			return loanResponse;
		}

		UserInitiativeRepamentRequest request = new UserInitiativeRepamentRequest();
		request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		request.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		request.setUserid(req.getOrgCode());
		request.setUserorderid(req.getOrderNo());

		LoanApplyOrder loanApplyOrder = getBaseLoanApplyInfo(req.getOrderNo(),
				req.getOrgCode());

		if (loanApplyOrder == null) {
			log.error("query loan apply order is null  [ orderNo = "
					+ req.getOrderNo() + "  orgCode=" + req.getOrgCode() + "]");
			loanResponse.putSysError();
			return loanResponse;
		}
		RepaymentAmountComputeRequest cReq = new RepaymentAmountComputeRequest();
		cReq.setAmount(req.getAmount());
		cReq.setOrderNo(req.getOrderNo());
		cReq.setOrgCode(req.getOrgCode());

		RepaymentAmountComputeResponse computeResponse = repaymentAmountCompute(cReq);

		if (!computeResponse.isResult()) {
			log.error("repayment amount compute fail.[ orderNo = "
					+ req.getOrderNo() + "  orgCode=" + req.getOrgCode() + "]");
			loanResponse.putSysError();
			return loanResponse;
		}

		request.setRealoverduecapitalamount(convetRepaymentAmount(computeResponse
				.getRealoverduecapitalAmount()));// 逾期本金还款金额

		request.setRealoverdueinterestamount(convetRepaymentAmount(+computeResponse
				.getRealoverdueinterestAmount()));// 逾期利息还款金额

		request.setRealinterestamount(convetRepaymentAmount(+computeResponse
				.getRealinterestAmount()));// 利息还款金额

		request.setRealcapitalamount(convetRepaymentAmount(+computeResponse
				.getRealcapitalAmount()));// 本金

		UserInitiativeRepamentResponse response = rsCreditManager
				.userInitiativeRepament(request);

		if (!response.isSuccess()) {

			log.error("initiative repament fail ! request["
					+ JsonConversionTool.toJson(request) + "] response["
					+ JsonConversionTool.toJson(response) + "]");

			loanResponse.putSysError();
			return loanResponse;
		}

		// this.synLoanOrderInfo(req.getOrderNo(), req.getOrgCode());
		loanResponse.putSuccess();

		return loanResponse;
	}

	@Override
	public GetLoanOrderInfoResponse getLoanOrderInfo(GetLoanOrderInfoRequest req) {

		GetLoanOrderInfoResponse infoResponse = new GetLoanOrderInfoResponse();

		LoanQueryConditions conditions = new LoanQueryConditions();
		conditions.setOrderNo(req.getOrderNo());
		conditions.setOrgCode(req.getOrgCode());

		PaginationSupport<LoanApplyOrder> paginationSupport = new PaginationSupport<LoanApplyOrder>();
		paginationSupport = loanOrderDao.queryLoanApplyOrder(conditions,
				paginationSupport);

		for (LoanApplyOrder loanApplyOrder : paginationSupport.getItemList()) {

			LoanApplyOrderBean loanRoomPackSpec = LoanTypeConvertUtil
					.getApplyOrderBean(loanApplyOrder);

			infoResponse.setApplyOrderInfo(loanRoomPackSpec);

			if (LoanProductEnum.ROOM_PACK.getCode() == loanRoomPackSpec
					.getProductType().intValue()) {

				LoanSpecification packSpec = new LoanSpecification();
				packSpec.setOrderNo(loanRoomPackSpec.getOrderNo());
				List<LoanSpecification> packSpecs = loanSpecificationDao
						.queryLoanSpecification(packSpec);

				if (packSpecs != null && packSpecs.size() > 0) {
					loanRoomPackSpec.setLoanSpec(LoanTypeConvertUtil
							.getLoanRoomPackSpecBean(packSpecs.get(0)));
				}
			} else {

				LoanSpecification packSpec = new LoanSpecification();
				packSpec.setOrderNo(loanRoomPackSpec.getOrderNo());
				List<LoanSpecification> packSpecs = loanSpecificationDao
						.queryLoanSpecification(packSpec);

				if (packSpecs != null && packSpecs.size() > 0) {
					loanRoomPackSpec.setLoanSpec(LoanTypeConvertUtil
							.getLoanSpecBean(packSpecs.get(0)));
				}

			}
			break;
		}
		infoResponse.setResult(true);
		return infoResponse;
	}

	/**
	 * 对字符为空的日期统一转成NULL
	 * 
	 * @param dateStr
	 * @return
	 */
	private Date convertDateUtil(String dateStr) {
		if (StringUtil.isValidString(dateStr)) {
			try {
				return DateUtil.sdf.parse(dateStr);
			} catch (ParseException e) {
				log.error("", e);
			}
		}
		return null;
	}

	/**
	 * 将请求转换呈查询条件对象
	 * 
	 * @param req
	 * @return
	 */
	private LoanQueryConditions getLoanQueryConditions(
			GetLoanOrderInfoListRequest req) {
		LoanQueryConditions conditions = new LoanQueryConditions();
		conditions.setOrgCode(req.getOrgCode());

		conditions.setBeginActualRepaymentDate(DateUtil
				.getDayBeginTime(convertDateUtil(req
						.getBeginActualRepaymentDate())));

		conditions.setBeginCreateTime(DateUtil
				.getDayBeginTime(convertDateUtil(req.getBeginCreateTime())));

		conditions.setBeginLastRepaymentDate(DateUtil
				.getDayBeginTime(convertDateUtil(req
						.getBeginLastRepaymentDate())));

		conditions.setBeginRelMoneyTime(DateUtil
				.getDayBeginTime(convertDateUtil(req.getBeginRelMoneyTime())));

		conditions
				.setEndActualRepaymentDate(DateUtil
						.getDayEndTime(convertDateUtil(req
								.getEndActualRepaymentDate())));

		conditions.setEndCreateTime(DateUtil.getDayEndTime(convertDateUtil(req
				.getEndCreateTime())));

		conditions.setEndLastRepaymentDate(DateUtil
				.getDayEndTime(convertDateUtil(req.getEndLastRepaymentDate())));

		conditions.setEndRelMoneyTime(DateUtil
				.getDayEndTime(convertDateUtil(req.getEndRelMoneyTime())));

		if (req.getProductEnum() != null) {
			conditions.setProductType(req.getProductEnum().getCode());
		}

		if (req.getOrderStatusEnum() != null
				&& req.getOrderStatusEnum().length > 0) {
			Integer is[] = new Integer[req.getOrderStatusEnum().length];
			for (int i = 0; i < is.length; i++) {
				is[i] = req.getOrderStatusEnum()[i].getKey();
			}
			conditions.setOrderStatusEnum(is);
		}
		return conditions;
	}

	/**
	 * 根据指定的查询条件查询对于的贷款单信息
	 */
	@Override
	public GetLoanOrderInfoListResponse getLoanOrderInfoList(
			GetLoanOrderInfoListRequest req) {

		GetLoanOrderInfoListResponse infoListResponse = new GetLoanOrderInfoListResponse();

		LoanQueryConditions conditions = getLoanQueryConditions(req);

		PaginationSupport<LoanApplyOrder> paginationSupport = new PaginationSupport<LoanApplyOrder>();
		paginationSupport.setPageSize(req.getPageSize());
		paginationSupport.setCurrentPage(req.getCurrentPage());
		paginationSupport.setOrderBy(req.getOrderBy());

		paginationSupport = loanOrderDao.queryLoanApplyOrder(conditions,
				paginationSupport);

		List<LoanApplyOrderBean> loanApplyOrderBeans = new ArrayList<LoanApplyOrderBean>();

		for (LoanApplyOrder loanApplyOrder : paginationSupport.getItemList()) {

			LoanApplyOrderBean applyOrderBean = LoanTypeConvertUtil
					.getApplyOrderBean(loanApplyOrder);
			loanApplyOrderBeans.add(applyOrderBean);
		}
		infoListResponse.setPageSize(paginationSupport.getPageSize());
		infoListResponse.setTotalCount(paginationSupport.getTotalCount());
		infoListResponse.setCurrentPage(paginationSupport.getCurrentPage());
		infoListResponse.setTotalPage(paginationSupport.getTotalPage());
		infoListResponse.setApplyOrderInfo(loanApplyOrderBeans);
		infoListResponse.setResult(true);
		return infoListResponse;
	}

	@Override
	public GetOrgLoanStatInfoResponse getOrgLoanStatInfo(
			GetOrgLoanStatInfoRequest req) {

		log.info("get loan stat info by orgCode=" + req.getOrgCode());

		GetOrgLoanStatInfoResponse rsp = new GetOrgLoanStatInfoResponse();

		OrgLoanStatInfo info = new OrgLoanStatInfo();

		rsp.setOrgLoanStatInfo(info);

		LoanExpiryStat expiryStat = loanOrderDao.queryLoanExpiryStat(req
				.getOrgCode());

		log.info("query expiry info [" + expiryStat + "]");

		LoanSevenDaysStat loanSevenDaysStat = loanOrderDao
				.queryLoanSevenDaysStat(req.getOrgCode());

		log.info("query SevenDay info [" + loanSevenDaysStat + "]");

		List<LoanProductAmountStat> amountStats = loanOrderDao
				.queryLoanProductAmountStat(req.getOrgCode());

		log.info("query product amount info [" + amountStats + "]");

		if (expiryStat != null) {
			info.setExpiryNum(expiryStat.getExpiryNum());
			info.setExpiryAmount(expiryStat.getExpiryAmount());
		}

		if (loanSevenDaysStat != null) {
			info.setSevenDaysAmount(loanSevenDaysStat.getSevenDaysAmount());
			info.setSevenDaysNum(loanSevenDaysStat.getSevenDaysNum());
		}

		if (amountStats != null && amountStats.size() > 0) {
			Map<LoanProductEnum, Long> statMap = new HashMap<LoanProductEnum, Long>();
			Map<LoanProductEnum, Long> statMap2 = new HashMap<LoanProductEnum, Long>();
			BigDecimal totalAmount = new BigDecimal(0);
			for (LoanProductAmountStat loanProductAmountStat : amountStats) {

				totalAmount = totalAmount.add(new BigDecimal(
						loanProductAmountStat.getAmount()));

				statMap.put(LoanProductEnum.getEnumByKey(loanProductAmountStat
						.getProductId()), loanProductAmountStat.getAmount());

				statMap2.put(LoanProductEnum.getEnumByKey(loanProductAmountStat
						.getProductId()), loanProductAmountStat
						.getActualAmount());
			}
			info.setLoanAmount(totalAmount.longValue());
			info.setProductAmount(statMap);
			info.setProductActualAmount(statMap2);
		}

		return rsp;
	}

	@Override
	public GetHistoryRepaymentListResponse getHistoryRepaymentList(
			GetHistoryRepaymentListRequest req) {

		GetHistoryRepaymentListResponse listResponse = new GetHistoryRepaymentListResponse();

		if (req == null || !StringUtil.isValidString(req.getOrderNo())
				|| !StringUtil.isValidString(req.getOrgCode())) {
			log.error("request param is null [ orderNo = " + req.getOrderNo()
					+ "  orgCode=" + req.getOrgCode() + "]");
			listResponse.putParamError();
			return listResponse;
		}

		QueryUserInitiativeRepaymentRequest request = new QueryUserInitiativeRepaymentRequest();
		request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		request.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		request.setUserid(req.getOrgCode());

		LoanApplyOrder loanApplyOrder = getBaseLoanApplyInfo(req.getOrderNo(),
				req.getOrgCode());

		if (loanApplyOrder == null) {
			log.error("query loan apply order is null  [ orderNo = "
					+ req.getOrderNo() + "  orgCode=" + req.getOrgCode() + "]");
			listResponse.putSysError();
			return listResponse;
		}

		request.setUserorderid(req.getOrderNo());
		QueryUserInitiativeRepaymentResponse response = rsCreditManager
				.queryUserInitiativeRepayment(request);

		LoanRepaymentBean repaymentBean = null;
		List<LoanRepaymentBean> loanRepaymentInfos = new ArrayList<LoanRepaymentBean>();
		// 实际贷款金额拿出来，然后按照每次还款减去对应的还款本金，计算出每次还款后剩下的本金
		BigDecimal actualAmount = new BigDecimal(
				loanApplyOrder.getActualAmount());
		if (response != null && response.gettUserArepaymentList() != null)

		{
			for (TUserArepayment arepayment : response.gettUserArepaymentList()) {
				if ("1".equals(arepayment.getStatusid())
						|| "4".equals(arepayment.getStatusid())) {
					repaymentBean = LoanTypeConvertUtil
							.getLoanRepaymentBean(arepayment);

					repaymentBean.setOrderNo(loanApplyOrder.getOrderNo());
					actualAmount = actualAmount.subtract(new BigDecimal(
							repaymentBean.getRepaymentAmount()));

					repaymentBean.setRemainAmount(actualAmount.longValue());
					loanRepaymentInfos.add(repaymentBean);
				}
			}
		}
		listResponse.setLoanRepaymentInfos(loanRepaymentInfos);
		listResponse.setResult(true);
		return listResponse;
	}

	@Override
	public LoanOrderNotifyResponse loanOrderNotify(LoanOrderNotifyRequest req) {
		LoanOrderNotifyResponse loanOrderNotifyResponse = new LoanOrderNotifyResponse();
		if (!StringUtil.isValidString(req.getOrderNo())) {
			loanOrderNotifyResponse.putErrorResult("贷款申请单号[orderNo]不能为空");
			return loanOrderNotifyResponse;
		}

		LoanQueryConditions loanQueryConditions = new LoanQueryConditions();
		loanQueryConditions.setOrderNo(req.getOrderNo());

		List<LoanApplyOrder> loanApplyOrderList = loanOrderDao
				.listLoanApplyOrder(loanQueryConditions);
		if (CollectionUtils.isEmpty(loanApplyOrderList)) {
			log.info("贷款通知处理->通知传过来的贷款申请订单不存在，贷款申请单号OrderNo:"
					+ req.getOrderNo());
			loanOrderNotifyResponse.putErrorResult("贷款订单不存在");
			return loanOrderNotifyResponse;
		}
		LoanApplyOrder loanApplyOrder = loanApplyOrderList.get(0);

		LoanProductDisposeAbstrator disposeAbstrator = loanProductDisposeFactory
				.getDisposeAbstrator(LoanProductEnum
						.getEnumByKey(loanApplyOrder.getProductType()));

		log.info("贷款通知处理->贷款通知处理，贷款单号OrderNo:" + req.getOrderNo());
		if (req.getState() == LoanOrderStatusEnum.AUDIT_PASS.getKey()
				|| req.getState() == LoanOrderStatusEnum.HAVE_LOAN.getKey()) {// 终审通过||放款成功

			if (req.getState() == LoanOrderStatusEnum.HAVE_LOAN.getKey()) {
				log.info("贷款通知处理->开始同步授信申请单信息，贷款单号OrderNo:" + req.getOrderNo()
						+ ",授信申请单号CreditOrderNo："
						+ loanApplyOrder.getCreditOrderNo());

				disposeAbstrator.loanSuccessNotify(loanApplyOrder.getOrderNo(),
						LoanOrderStatusEnum.getEnumByStatus(req.getState()));

				// 同步剩余可用授信金额
				SynLoanCreditOrderRequest creditOrderRequest = new SynLoanCreditOrderRequest();
				creditOrderRequest.setOrgCode(loanApplyOrder.getOrgCode());
				creditOrderRequest
						.setOrderNo(loanApplyOrder.getCreditOrderNo());
				loanCreditService.synLoanCreditOrder(creditOrderRequest);

			} else if (req.getState() == LoanOrderStatusEnum.AUDIT_PASS
					.getKey()) {
				disposeAbstrator.loanAuditPassNotify(
						loanApplyOrder.getOrderNo(),
						LoanOrderStatusEnum.getEnumByStatus(req.getState()));
			}

			SynLoanOrderRequest synLoanOrderRequest = new SynLoanOrderRequest();
			synLoanOrderRequest.setOrderNo(req.getOrderNo());
			synLoanOrderRequest.setOrgCode(loanApplyOrder.getOrgCode());
			log.info("贷款通知处理->同步贷款订单信息开始处理，贷款单号OrderNo:" + req.getOrderNo());
			this.synLoanOrderInfo(synLoanOrderRequest);
			log.info("贷款通知处理->同步贷款订单信息完成，贷款单号OrderNo:" + req.getOrderNo());

		} else {

			disposeAbstrator.loanFailNotify(loanApplyOrder.getOrderNo(),
					LoanOrderStatusEnum.getEnumByStatus(req.getState()),
					req.getMsg());
		}

		loanOrderNotifyResponse.putSuccess("通知处理成功");
		// 发送短信通知
		sendLoanSms(req.getOrderNo());
		return loanOrderNotifyResponse;
	}

	/**
	 * 确认贷款单信息在平台是否存在
	 * 
	 * @param req
	 * @return
	 */
	public ConfirmLoanOrderIsAvailableResponse confirmLoanOrderIsAvailable(
			ConfirmLoanOrderIsAvailableRequest req) {
		ConfirmLoanOrderIsAvailableResponse listResponse = new ConfirmLoanOrderIsAvailableResponse();

		if (req == null || !StringUtil.isValidString(req.getOrderNo())
				|| !StringUtil.isValidString(req.getOrgCode())) {
			log.error("request param is null [ orderNo = " + req.getOrderNo()
					+ "  orgCode=" + req.getOrgCode() + "]");
			listResponse.putParamError();
			return listResponse;
		}

		// 查询贷款单信息
		QueryLoanApplyRequest request = new QueryLoanApplyRequest();
		request.setRootinstcd(CommonConstant.RS_FANGCANG_CONST_ID);
		request.setProductid(LoanApplyOrderEnum.ProductId.MAIN_PRODUCTID.productId);
		request.setUserorderid(req.getOrderNo());
		request.setUserid(req.getOrgCode());

		QueryLoanApplyResponse rsp = rsCreditManager.queryLoanApply(request);

		if (!rsp.isSuccess()) {
			log.error("query Loan Apply info fail.  [ orderNo = "
					+ req.getOrderNo() + "  orgCode=" + req.getOrgCode() + "]");
			listResponse.putSysError();
			return listResponse;
		}
		listResponse.putSuccess();
		return listResponse;
	}

	/**
	 * 离线贷款申请
	 * 
	 * @param req
	 * @return
	 */
	public OfflineLoanApplyResponse offlineLoanApply(OfflineLoanApplyRequest req) {

		OfflineLoanApplyResponse response = new OfflineLoanApplyResponse();

		LoanProductEnum productType = req.getProductType();

		LoanCreditOrder loanCreditOrder = new LoanCreditOrder();
		loanCreditOrder.setOrgCode(req.getOrgCode());
		List<LoanCreditOrder> loanCreditOrderList = loanCreditOrderDao
				.queryLoanCreditOrder(loanCreditOrder);
		if (loanCreditOrderList == null || loanCreditOrderList.size() != 1) {
			response.putErrorResult("query credit order is null");
			return response;
		}

		loanCreditOrder = loanCreditOrderList.get(0);

		// 保存相关数据
		try {
			this.saveLoanSpecBean(req.getLcanSpec());
		} catch (Exception e) {
			log.error("", e);
			response.putErrorResult("save loan spec fail!");
			return response;
		}

		// 保存贷款申请单
		try {
			saveLoanOrderBean(req.getLcanSpec(), productType.getCode(),
					req.getOrgCode(), loanCreditOrder.getOrderNo(), 0);
		} catch (Exception e) {
			log.error("", e);
			response.putErrorResult("save loan info fail!");
			return response;
		}

		response.putSuccess();

		return response;
	}

	@Override
	public SaveLoanOrderInfoResponse saveLoanOrderInfo(
			SaveLoanOrderInfoRequest req) {
		return null;
	}

}
