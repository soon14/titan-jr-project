package com.fangcang.titanjr.web.controller.admin;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fangcang.titanjr.common.enums.CoopTypeEnum;
import com.fangcang.titanjr.common.enums.OrgCheckResultEnum;
import com.fangcang.titanjr.common.enums.entity.TitanOrgEnum;
import com.fangcang.titanjr.common.exception.GlobalServiceException;
import com.fangcang.titanjr.common.exception.MessageServiceException;
import com.fangcang.titanjr.dto.BaseResponseDTO;
import com.fangcang.titanjr.dto.bean.FinancialOrganDTO;
import com.fangcang.titanjr.dto.bean.OrgBindInfo;
import com.fangcang.titanjr.dto.request.CancelOrgBindRequest;
import com.fangcang.titanjr.dto.request.FinancialOrganQueryRequest;
import com.fangcang.titanjr.dto.request.OrganCheckRequest;
import com.fangcang.titanjr.dto.response.FinancialOrganResponse;
import com.fangcang.titanjr.dto.response.OrganCheckResponse;
import com.fangcang.titanjr.dto.response.OrganQueryCheckResponse;
import com.fangcang.titanjr.service.TitanFinancialOrganService;
import com.fangcang.titanjr.service.TitanFinancialUserService;
import com.fangcang.titanjr.web.controller.BaseController;
import com.fangcang.titanjr.web.pojo.OrgPojo;
import com.fangcang.titanjr.web.util.WebConstant;
import com.fangcang.util.StringUtil;

import net.sf.json.JSONSerializer;

/**
 * 金融机构审核
 * @author luoqinglong
 * @2016年6月12日
 */
@Controller
@RequestMapping("/admin")
public class OrgController extends BaseController{
	private static final Log logger = LogFactory.getLog(OrgController.class);
	@Resource
	private TitanFinancialOrganService organService;
	@Resource
	private TitanFinancialUserService userService;
	
	/**
	 * 企业机构页面
	 * @return
	 */
	@RequestMapping(value = "/org")
	public String org(Integer userType,Model model){
		if(userType==null||userType==1){
			//企业
			return "admin/org/en-verify-org-list";
		}
		if(userType==2){
			//个人
			return "admin/org/per-verify-org-list";
		}
		//默认
		return "admin/org/en-verify-org-list";
	}
	
	/***
	 * 机构查询
	 * @param orgPojo
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/orgTable")
	public String orgTable(OrgPojo orgPojo, Model model) throws Exception{
		
		int userType = orgPojo.getUserType()==null?1:orgPojo.getUserType();
		
		FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
		organQueryRequest.setUserloginname(orgPojo.getUserLoginName());
		organQueryRequest.setSubUserType(userType);
		organQueryRequest.setSubOrgName(orgPojo.getOrgName());
		organQueryRequest.setResultKey("".equals(orgPojo.getResultKey())?null:orgPojo.getResultKey());
		organQueryRequest.setCurrentPage(orgPojo.getPageNo());
		organQueryRequest.setPageSize(orgPojo.getPageSize());
		
		OrganQueryCheckResponse organCheckResponse = organService.queryFinancialOrganForPage(organQueryRequest);
		if(organCheckResponse.isResult()){
			model.addAttribute("orgCheckDTOPage", organCheckResponse.getPaginationSupport());
		}else{
			logger.error("机构查询失败,参数:"+JSONSerializer.toJSON(orgPojo).toString());
			throw new Exception("机构查询失败");
		}
		count(model);
		if(userType==1){
			return "admin/org/en-verify-org-table";
		}else{
			return "admin/org/per-verify-org-table";
		}
		
	}
	
	/**
	 * 审核和查看机构（企业）
	 * @param orgId
	 * @return
	 */
	@RequestMapping(value = "/viewOrg")
	public String viewOrg(Integer orgId,String opt,String tfsLoginUsername, Model model){
		if(orgId==null||opt==null){
			model.addAttribute("errormsg", "参数错误");
			return "error";
		}
		model.addAttribute("opt", opt);
		
		FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
		organQueryRequest.setOrgId(orgId);
		//优化改为通用的查询，不使用机构绑定关系查询条件
		FinancialOrganResponse financialOrganResponse = organService.queryBaseFinancialOrgan(organQueryRequest);
		if(financialOrganResponse.isResult()){
			FinancialOrganDTO financialOrganDTO = financialOrganResponse.getFinancialOrganDTO();
			model.addAttribute("financialOrganDTO", financialOrganDTO);
			model.addAttribute("orgSubDTO", financialOrganResponse.getOrgSubDTO());
			if(!CollectionUtils.isEmpty(financialOrganDTO.getOrgImageInfoList())){
				model.addAttribute("orgImgUrl", financialOrganDTO.getOrgImageInfoList().get(0).getImageURL());
			}
			
			model.addAttribute("operatorName", getSAASLoginName());//TODO 这里是个bug,应该从数据库里面拿
			model.addAttribute("tfsLoginUsername", tfsLoginUsername);
			
			if(financialOrganResponse.getOrgSubDTO().getUserType()==1){
				return "admin/org/en-verify-org-info";
			}else {
				return "admin/org/per-verify-org-info";
			}
		}else{
			model.addAttribute("errormsg", financialOrganResponse.getReturnMessage());
			return "error";
		}
	}
	
	
	
	/**
	 * 提交审核
	 * @param checkStatus 审核结果
	 * @param reason 原因
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkOrg")
	public String checkOrg(Integer checkStatus,String reason,Integer orgId){
		if(orgId==null||orgId<=0||checkStatus==null){
			putSysError("参数错误");
			return toJson();
		}
		OrganCheckRequest organCheckRequest = new OrganCheckRequest();
		organCheckRequest.setCheckstatus(checkStatus);
		organCheckRequest.setOrgId(orgId);
		organCheckRequest.setResultMsg(reason);
		organCheckRequest.setOperator(getSAASLoginName());
		
		try {
			OrganCheckResponse organCheckResponse = organService.checkFinancialOrgan(organCheckRequest);
			if(organCheckResponse.isResult()){
				putSuccess("审核成功");
				
				return toJson();
			}else{
				putSysError(organCheckResponse.getReturnMessage());
				return toJson();
			}
		} catch (GlobalServiceException e) {
			logger.error("机构审核失败,参数:orgId["+orgId+"],checkStatus["+checkStatus+"],reason["+reason+"]",e);
			putSysError(WebConstant.CONTROLLER_ERROR_MSG);
			return toJson();
			
		} catch (MessageServiceException e) {
			logger.error("机构审核失败,参数:orgId["+orgId+"],checkStatus["+checkStatus+"],reason["+reason+"]",e);
			putSysError(e.getMessage());
			return toJson();
		}
		
	}
	/***
	 * 待审核数
	 */
	private void count(Model model){
		FinancialOrganQueryRequest enterpriseRequest = new FinancialOrganQueryRequest();
		enterpriseRequest.setResultKey(OrgCheckResultEnum.FT.getResultkey());
		enterpriseRequest.setSubUserType(TitanOrgEnum.UserType.ENTERPRISE.getKey());
		int enCount = organService.countOrg(enterpriseRequest);
		model.addAttribute("enCount", enCount);
		
		FinancialOrganQueryRequest personalRequest = new FinancialOrganQueryRequest();
		personalRequest.setResultKey(OrgCheckResultEnum.FT.getResultkey());
		personalRequest.setSubUserType(TitanOrgEnum.UserType.PERSONAL.getKey());
		int perCount = organService.countOrg(personalRequest);
		model.addAttribute("perCount", perCount);
	}
	
	/**
	 * 取消机构绑定
	 * @param orgCode
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/cancelOrgBind")
	public String cancelOrgBind(String orgCode){
		if(!StringUtil.isValidString(orgCode)){
			putSysError("参数错误不能为空");
			return toJson();
		}
		try {
			//暂时只支持SaaS注册的金融账户
			OrgBindInfo orgBindInfo = new OrgBindInfo();
			orgBindInfo.setOrgcode(orgCode);
			orgBindInfo = organService.queryOrgBindInfoByUserid(orgBindInfo);
			if(orgBindInfo==null){
				putSysError("该金融账户的绑定关系不存在");
				return toJson();
			}
			if(!orgBindInfo.getCoopType().equals(CoopTypeEnum.SAAS.getKey())){
				putSysError("暂时只支持SaaS注册的金融账户");
				return toJson();
			}
			CancelOrgBindRequest cancelOrgBindRequest = new CancelOrgBindRequest();
			cancelOrgBindRequest.setOrgCode(orgCode);
			cancelOrgBindRequest.setOperator(getSAASLoginName());
			BaseResponseDTO baseResponseDTO = organService.cancelOrgBind(cancelOrgBindRequest); 
			if(baseResponseDTO.isResult()){
				putSuccess("取消成功");
				return toJson();
			}else{
				putSysError(baseResponseDTO.getReturnMessage());
				return toJson();
			}
		} catch (MessageServiceException e) {
			logger.error("取消机构绑定操作失败，orgCode:"+orgCode+"，失败原因"+e.getMessage() ,e);
			putSysError(e.getMessage());
			return toJson();
		}
	}
}
