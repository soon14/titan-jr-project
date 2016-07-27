package com.fangcang.titanjr.service.impl;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fangcang.message.email.dto.EmailSenderDTO;
import com.fangcang.message.email.service.EmailSendService;
import com.fangcang.message.sms.dto.SMSSendDTO;
import com.fangcang.message.sms.service.MessageSendService;
import com.fangcang.titanjr.common.enums.RSInvokeErrorEnum;
import com.fangcang.titanjr.common.factory.HessianProxyBeanFactory;
import com.fangcang.titanjr.common.factory.ProxyFactoryConstants;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.DubboServerJDBCProperties;
import com.fangcang.titanjr.common.util.Tools;
import com.fangcang.titanjr.dto.request.SendCodeRequest;
import com.fangcang.titanjr.dto.request.SendSMSRequest;
import com.fangcang.titanjr.dto.response.SendCodeResponse;
import com.fangcang.titanjr.dto.response.SendSmsResponse;
import com.fangcang.titanjr.service.TitanFinancialSendSMSService;
import com.fangcang.util.StringUtil;

@Service("titanFinancialSendSMSService")
public class TitanFinancialSendSMSServiceImpl implements TitanFinancialSendSMSService {
	private static final Log log = LogFactory.getLog(TitanFinancialSendSMSServiceImpl.class);
	 
	@Autowired
	private DubboServerJDBCProperties dubboServerJDBCProperties;
	
	@Autowired
    private HessianProxyBeanFactory hessianProxyBeanFactory ;

	@Override
	public SendSmsResponse sendSMS(SendSMSRequest sendSMSRequest) {
		SendSmsResponse sendSmsResponse = new SendSmsResponse();
		try{
			if(sendSMSRequest !=null){
				SMSSendDTO smsSendDTO = new SMSSendDTO();
				smsSendDTO.setMobiles(sendSMSRequest.getMobilePhone());
				smsSendDTO.setContent(sendSMSRequest.getContent());
				smsSendDTO.setProviderkey(sendSMSRequest.getProviderkey());
				smsSendDTO.setMerchantCode(sendSMSRequest.getMerchantCode());
				String messageServiceUrl= ProxyFactoryConstants.messageServiceUrl + "messageSendService";
				MessageSendService messageSendService = hessianProxyBeanFactory.getHessianProxyBean(MessageSendService.class,messageServiceUrl);
			    String retMessage = messageSendService.sendSMS(smsSendDTO);
			    if(CommonConstant.RETURN_SUCCESS.toUpperCase().equals(retMessage)){
			    	sendSmsResponse.setResult(true);
				    sendSmsResponse.setReturnCode(RSInvokeErrorEnum.INVOKE_SUCCESS.returnCode);
				    sendSmsResponse.setReturnMessage(RSInvokeErrorEnum.INVOKE_SUCCESS.returnMsg);
				    return sendSmsResponse;
			    }else{
			    	log.error("send sms ,url:"+messageServiceUrl+",param:"+ToStringBuilder.reflectionToString(smsSendDTO)+",retMessage:"+retMessage);
			    	sendSmsResponse.setResult(false);
					sendSmsResponse.setReturnCode(RSInvokeErrorEnum.UNKNOWN_ERROR.returnCode);
					sendSmsResponse.setReturnMessage(retMessage);
					return sendSmsResponse;
			    }
			}else{
				sendSmsResponse.setResult(false);
				sendSmsResponse.setReturnCode(RSInvokeErrorEnum.PARAM_EMPTY.returnCode);
				sendSmsResponse.setReturnMessage(RSInvokeErrorEnum.PARAM_EMPTY.returnMsg);
				return sendSmsResponse;
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
		sendSmsResponse.setResult(false);
		sendSmsResponse.setReturnCode(RSInvokeErrorEnum.UNKNOWN_ERROR.returnCode);
		sendSmsResponse.setReturnMessage(RSInvokeErrorEnum.UNKNOWN_ERROR.returnMsg);
		return sendSmsResponse;
	}

	@Override
	public SendCodeResponse sendCode(SendCodeRequest sendCodeRequest) {
		//参数校验
		SendCodeResponse response = new SendCodeResponse();
		
		if(!StringUtil.isValidString(sendCodeRequest.getReceiveAddress())){
			response.putErrorResult("接收地址不能为空"); 
			return response;
		}
		if(!StringUtil.isValidString(sendCodeRequest.getMerchantCode())){
			response.putErrorResult("发送方的商家编码不能为空"); 
			return response;
		}
		int sendType = sendCodeRequest.getReceiveAddress().indexOf("@")>-1?2:1;
		
		//1.短信
		if(sendType==1){
			if(Tools.isNotPhone(sendCodeRequest.getReceiveAddress())){
				response.putErrorResult("手机号码格式不正确"); 
				return response;
			}
			SendSMSRequest sendSMSRequest = new SendSMSRequest();
			sendSMSRequest.setContent(sendCodeRequest.getContent());
			sendSMSRequest.setMerchantCode(sendCodeRequest.getMerchantCode());
			sendSMSRequest.setMobilePhone(sendCodeRequest.getReceiveAddress());
			if(StringUtil.isValidString(sendCodeRequest.getProviderkey())){
				sendSMSRequest.setProviderkey(sendCodeRequest.getProviderkey());
			}else{
				sendSMSRequest.setProviderkey(CommonConstant.DEFAULT_SMS_PROVIDER_KEY);
			}
			SendSmsResponse sendSmsResponse = sendSMS(sendSMSRequest);
			if(sendSmsResponse.isResult()){
				response.putSuccess("短信发送成功");
			}else{
				response.putErrorResult(sendSmsResponse.getReturnCode(),sendSmsResponse.getReturnMessage());
			}
			return response;
		}else {
			//邮件
			if(Tools.isNotEmailAddress(sendCodeRequest.getReceiveAddress())){
				response.putErrorResult("邮箱地址不正确"); 
				return response;
			}
			if(!StringUtil.isValidString(sendCodeRequest.getSubject())){
				response.putErrorResult("邮件主题不能为空"); 
				return response;
			}
			
			String messageServiceUrl= ProxyFactoryConstants.messageServiceUrl + "emailSendService";
			EmailSenderDTO emailSenderDTO  = new EmailSenderDTO();
			emailSenderDTO.setTo(sendCodeRequest.getReceiveAddress());
			emailSenderDTO.setContent(sendCodeRequest.getContent());
			emailSenderDTO.setSubject(sendCodeRequest.getSubject());
			emailSenderDTO.setFrom(dubboServerJDBCProperties.getJrEmailFrom());
			emailSenderDTO.setUserName(dubboServerJDBCProperties.getJrEmailUsername());
			emailSenderDTO.setPassword(dubboServerJDBCProperties.getJrEmailPassword());
			emailSenderDTO.setEmailServerHost(dubboServerJDBCProperties.getJrEmailServer());
			emailSenderDTO.setEmailServerPort(dubboServerJDBCProperties.getJrEmailPort());
			//TODO 修改正式环境的商家
			emailSenderDTO.setMerchantCode(CommonConstant.FANGCANG_MERCHANTCODE);
			try {
				EmailSendService emailSendService = hessianProxyBeanFactory.getHessianProxyBean(EmailSendService.class,messageServiceUrl);
				boolean  retMessage = emailSendService.send(emailSenderDTO);
				if(retMessage){
					 response.putSuccess("邮件发送成功");
					 return response;
				}else{
					log.error("send email code ,url:"+messageServiceUrl+",param:"+ToStringBuilder.reflectionToString(emailSenderDTO)+",retMessage:"+retMessage);
					response.putErrorResult("邮件发送失败");
				}
			} catch (Exception e) {
				log.error("邮件发送失败,sendEmailRequest:"+ToStringBuilder.reflectionToString(sendCodeRequest),e);
				response.putErrorResult("服务异常，邮件发送失败");
			}
		}
		return response;
	}
}
