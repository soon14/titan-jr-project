/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName TLAgentTradeServiceImpl.java
 * @author Jerry
 * @date 2017年12月25日 下午6:30:36  
 */
package com.titanjr.checkstand.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fangcang.titanjr.common.enums.WithDrawStatusEnum;
import com.titanjr.checkstand.constants.RSErrorCodeEnum;
import com.titanjr.checkstand.constants.SysConstant;
import com.titanjr.checkstand.dto.TLAgentInfoResponseDTO;
import com.titanjr.checkstand.dto.TitanAgentResDetailDTO;
import com.titanjr.checkstand.request.TLAgentTradeRequest;
import com.titanjr.checkstand.respnse.TLAgentTradeResponse;
import com.titanjr.checkstand.respnse.TitanAgentPayResponse;
import com.titanjr.checkstand.respnse.TitanAgentQueryResponse;
import com.titanjr.checkstand.service.TLAgentTradeService;
import com.titanjr.checkstand.util.tlUtil.XmlTools;
import com.titanjr.checkstand.util.tlUtil.bean.QTDetail;
import com.titanjr.checkstand.util.tlUtil.bean.QTransRsp;
import com.titanjr.checkstand.util.tlUtil.bean.TransRet;


/**
 * 通联账户交易服务实现 
 * @author Jerry
 * @date 2017年12月25日 下午6:30:36  
 */
@Service("tlAgentTradeService")
public class TLAgentTradeServiceImpl implements TLAgentTradeService {
	
	private static final Logger logger = LoggerFactory.getLogger(TLAgentTradeServiceImpl.class);
	private final String resUrl = this.getClass().getResource("/").getPath().replace("classes/", "");
	
	@Override
	public TitanAgentPayResponse agentPay(TLAgentTradeRequest tlAgentTradeRequest) {
		
		TitanAgentPayResponse titanAgentPayResponse = new TitanAgentPayResponse();
		
		try{
			
			String resp = sendXml(tlAgentTradeRequest);
			titanAgentPayResponse = agentPayXmlResult(resp);
			
		}catch(Exception e){
			
			if(e.getCause() instanceof ConnectException||e instanceof ConnectException){
				logger.error("【通联-代付请求】请求链接中断，请做交易结果查询，以确认该笔交易是否已被通联受理，避免重复交易");
			}
			logger.error("【通联-代付请求】异常：", e);
			titanAgentPayResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
		}
		
		return titanAgentPayResponse;
		
	}
	
	
	@Override
	public TitanAgentQueryResponse agentQuery(TLAgentTradeRequest tlAgentTradeRequest) {

		TitanAgentQueryResponse titanAgentTradeResponse = new TitanAgentQueryResponse();
		
		try{
			
			String resp = sendXml(tlAgentTradeRequest);
			titanAgentTradeResponse = agentQuery(resp);
			
		}catch(Exception e){
			
			if(e.getCause() instanceof ConnectException||e instanceof ConnectException){
				logger.error("【通联-账户交易查询】请求链接中断");
			}
			logger.error("【通联-账户交易查询】异常：", e);
			titanAgentTradeResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
		}
		
		return titanAgentTradeResponse;
		
	}
	
	
	@Override
	public void agentDownload(TLAgentTradeRequest tlAgentTradeRequest) {
		
		try{
			
			String resp = sendXml(tlAgentTradeRequest);
			writeBill(resp);
			
		}catch(Exception e){
			
			if(e.getCause() instanceof ConnectException||e instanceof ConnectException){
				logger.error("【通联-对账文件下载】请求链接中断");
			}
			logger.error("【通联-对账文件下载】异常：", e);
		}
		
	}
	
	
	/**
	 * 发送报文
	 * @author Jerry
	 * @date 2017年12月28日 上午11:29:49
	 */
	private String sendXml(TLAgentTradeRequest tlAgentTradeRequest) throws Exception{
		
		String xml = XmlTools.buildXml(tlAgentTradeRequest, true);
		
		xml = XmlTools.signMsg(xml, resUrl+SysConstant.PFX_PATH, SysConstant.PFX_PWD, false);
		
		logger.info("======================发送报文======================：\n{}", xml);
		String resp = XmlTools.send(SysConstant.AGENT_TRADE_URL, xml);
		logger.info("======================响应内容======================") ;
		
		boolean flag = XmlTools.verifySign(resp, resUrl+SysConstant.CER_PATH, false, false);
		logger.info("验签结果[{}]", flag) ;
		
		return resp;
		
	}
	
	
	/**
	 * 代付返回报文处理逻辑
	 * @author Jerry
	 * @date 2017年12月26日 下午3:37:53
	 */
	private TitanAgentPayResponse agentPayXmlResult(String retXml) {
		
		String trxcode = null;
		TitanAgentPayResponse titanAgentPayResponse = new TitanAgentPayResponse();
		TLAgentTradeResponse tlAgentTradeResponse = null;
		//或者交易码
		if (retXml.indexOf("<TRX_CODE>") != -1) {
			int end = retXml.indexOf("</TRX_CODE>");
			int begin = end - 6;
			if (begin >= 0) {
				trxcode = retXml.substring(begin, end);
			}
		}
		tlAgentTradeResponse = XmlTools.parseRsp(retXml);
		TLAgentInfoResponseDTO info = tlAgentTradeResponse.getINFO();
		TransRet ret = (TransRet) tlAgentTradeResponse.getTrxData().get(0);
		titanAgentPayResponse.setOrderNo(info.getREQ_SN());
		titanAgentPayResponse.setTradeCode(info.getTRX_CODE());
		
		//实时交易结果返回处理逻辑(包括单笔实时代收，单笔实时代付，单笔实时身份验证)
		if("100011".equals(trxcode)||"100014".equals(trxcode)||"100400".equals(trxcode)){
			if("0000".equals(tlAgentTradeResponse.getINFO().getRET_CODE())){
				
				logger.info("提交成功，交易结果：{}：{}", ret.getRET_CODE(), ret.getERR_MSG());
				if("0000".equals(ret.getRET_CODE())){
					logger.info("交易成功（最终结果）");
					titanAgentPayResponse.setStatus(WithDrawStatusEnum.WithDraw_SUCCESSED.getKey().toString());
				}else{
					logger.info("交易失败（最终结果），原因："+ret.getERR_MSG());
					titanAgentPayResponse.setStatus(WithDrawStatusEnum.WithDraw_FAILED.getKey().toString());
				}
				
			}else if("2000".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2001".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2003".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2005".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2007".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2008".equals(tlAgentTradeResponse.getINFO().getRET_CODE())){
				
				logger.info("交易处理中或者不确定状态，需要在稍后5分钟后进行交易结果查询（轮询）");
				titanAgentPayResponse.setStatus(WithDrawStatusEnum.WithDraw_DOING.getKey().toString());
				
			}else if(tlAgentTradeResponse.getINFO().getRET_CODE().startsWith("1")){
				
				String errormsg = tlAgentTradeResponse.getINFO().getERR_MSG()==null ? "连接异常，请重试" : tlAgentTradeResponse.getINFO().getERR_MSG();
				logger.error("交易请求失败，原因：{}", errormsg);
				titanAgentPayResponse.putErrorResult(RSErrorCodeEnum.build(errormsg));
				
			}else{
				
				logger.error("交易失败(最终结果)，失败原因：", ret.getERR_MSG());
				titanAgentPayResponse.putErrorResult(RSErrorCodeEnum.build(ret.getERR_MSG()));
				
			}
		}
		
		return titanAgentPayResponse;
		
	}
	
	
	/**
	 * 交易查询返回报文处理
	 * @author Jerry
	 * @date 2017年12月28日 下午3:40:24
	 */
	@SuppressWarnings("unchecked")
	private TitanAgentQueryResponse agentQuery(String retXml) {
		
		String trxcode = null;
		TitanAgentQueryResponse titanAgentTradeResponse = new TitanAgentQueryResponse();
		TLAgentTradeResponse tlAgentTradeResponse = null;
		//或者交易码
		if (retXml.indexOf("<TRX_CODE>") != -1)
		{
			int end = retXml.indexOf("</TRX_CODE>");
			int begin = end - 6;
			if (begin >= 0) trxcode = retXml.substring(begin, end);
		}
		tlAgentTradeResponse = XmlTools.parseRsp(retXml);
		titanAgentTradeResponse.setTradeCode(tlAgentTradeResponse.getINFO().getTRX_CODE());
		
		//交易查询处理逻辑
		if("200004".equals(trxcode)||"200005".equals(trxcode)){
			if("0000".equals(tlAgentTradeResponse.getINFO().getRET_CODE())){
				
				titanAgentTradeResponse.setRetCode(SysConstant.RET_CODE_SUCCESS);
				QTransRsp qrsq = (QTransRsp) tlAgentTradeResponse.getTrxData().get(0);
				logger.info("查询成功，具体结果明细如下:");
				List<TitanAgentResDetailDTO> titanDetails = new ArrayList<TitanAgentResDetailDTO>();
				List<QTDetail> details = qrsq.getDetails();
				
				for(QTDetail qTDetail : details){
					
					logger.info(qTDetail.toString());
					TitanAgentResDetailDTO resDetail = new TitanAgentResDetailDTO();
					resDetail.setOrderNo(qTDetail.getBATCHID());
					resDetail.setTradeType(qTDetail.getTRXDIR());
					resDetail.setTradeAmount(qTDetail.getAMOUNT());
					resDetail.setAccountNo(qTDetail.getACCOUNT_NO());
					resDetail.setAccountName(qTDetail.getACCOUNT_NAME());
					resDetail.setClearDate(qTDetail.getSETTDAY());
					resDetail.setSubmitTime(qTDetail.getSUBMITTIME());
					resDetail.setRemark(qTDetail.getREMARK());
					
					if("0000".equals(qTDetail.getRET_CODE())){
						
						logger.info("返回说明:交易成功  ");
						resDetail.setTradeStatus(WithDrawStatusEnum.WithDraw_SUCCESSED.getKey().toString());
						
					}else{
						
						logger.info("返回说明：{}", qTDetail.getERR_MSG());
						resDetail.setTradeStatus(WithDrawStatusEnum.WithDraw_FAILED.getKey().toString());
						
					}
					titanDetails.add(resDetail);
				}
				titanAgentTradeResponse.setDetails(titanDetails);
				
			} else if("2000".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2001".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2003".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2005".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2007".equals(tlAgentTradeResponse.getINFO().getRET_CODE())
					||"2008".equals(tlAgentTradeResponse.getINFO().getRET_CODE())){
				
				logger.info("整个批次的交易都是处理中：{}", tlAgentTradeResponse.getINFO().getERR_MSG());
				titanAgentTradeResponse.setRetCode(SysConstant.RET_CODE_PROCESS);
				
			} else if("2004".equals(tlAgentTradeResponse.getINFO().getRET_CODE())){
				
				logger.info("整个批次都未受理通过（最终失败）");
				titanAgentTradeResponse.setRetCode(SysConstant.RET_CODE_NOT_FIND);
				
			} else if("1002".equals(tlAgentTradeResponse.getINFO().getRET_CODE())){
				
				logger.info("未查询到对应的交易");
				titanAgentTradeResponse.setRetCode(SysConstant.RET_CODE_NOT_FIND);
				
			} else{
				
				logger.error("查询请求失败，请重新发起查询：{}", tlAgentTradeResponse.getINFO().getERR_MSG());
				titanAgentTradeResponse.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				
			}
		}
		
		return titanAgentTradeResponse;
		
	}
	
	
	/**
	 * 解析并写入对账文件
	 * @author Jerry
	 * @date 2017年12月29日 上午10:02:05
	 */
	@SuppressWarnings("resource")
	private void writeBill(String resp) throws Exception {
		
		int iStart = resp.indexOf("<CONTENT>");
		int end = resp.indexOf("</CONTENT>");
		if(iStart==-1) {
			throw new Exception("XML报文中不存在<CONTENT>");
		}
		if(end==-1) {
			throw new Exception("XML报文中不存在</CONTENT>");	
		}
		String billContext = resp.substring(iStart + 9, end);
		
		//写文件
		FileOutputStream sos=null;
		sos=new FileOutputStream(new File(resUrl+"/bills/bill.gz"));
		Base64InputStream b64is=new Base64InputStream(IOUtils.toInputStream(billContext),false);
		IOUtils.copy(b64is, sos);
		IOUtils.closeQuietly(b64is);
		
		//解压
		ZipInputStream zin=new ZipInputStream(new FileInputStream(new File(resUrl+"/bills/bill.gz")));
		ZipEntry zipEntry=null;
		while ((zipEntry = zin.getNextEntry()) != null) {  
			 String entryName = zipEntry.getName().toLowerCase(); 
			 FileOutputStream os = new FileOutputStream(resUrl+"/bills/"+entryName);  
             // Transfer bytes from the ZIP file to the output file  
             byte[] buf = new byte[1024];  
             int len;  
             while ((len = zin.read(buf)) > 0) {  
                 os.write(buf, 0, len);  
             }  
             os.close();  
             zin.closeEntry();
		 }
		
		logger.info("对账文件下载成功");
	}

}