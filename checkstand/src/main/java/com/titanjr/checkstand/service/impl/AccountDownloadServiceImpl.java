/** 
 * CopyRright ©2017 深圳市天下房仓科技有限公司 All Right Reserved.
 * 
 * @fileName AccountDownloadServiceImpl.java
 * @author Jerry
 * @date 2018年3月15日 下午5:17:15  
 */
package com.titanjr.checkstand.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.allinpay.ets.client.SecurityUtil;
import com.fangcang.titanjr.common.util.FtpUtil;
import com.fangcang.titanjr.common.util.MD5;
import com.fangcang.titanjr.common.util.httpclient.HttpClient;
import com.fangcang.titanjr.dto.response.FTPConfigResponse;
import com.fangcang.titanjr.service.TitanSysconfigService;
import com.fangcang.util.DateUtil;
import com.fangcang.util.JsonUtil;
import com.fangcang.util.StringUtil;
import com.titanjr.checkstand.constants.RBTradeTypeEnum;
import com.titanjr.checkstand.constants.GatewayDwonloadErrCodeEnum;
import com.titanjr.checkstand.constants.PayTypeEnum;
import com.titanjr.checkstand.constants.RSErrorCodeEnum;
import com.titanjr.checkstand.constants.SysConstant;
import com.titanjr.checkstand.constants.TLTradeTypeEnum;
import com.titanjr.checkstand.dao.AccountDownloadDao;
import com.titanjr.checkstand.dto.AccountDownloadDTO;
import com.titanjr.checkstand.dto.GateWayConfigDTO;
import com.titanjr.checkstand.dto.TLAgentQueryTransDTO;
import com.titanjr.checkstand.request.RBAgentDownloadRequest;
import com.titanjr.checkstand.request.TLAgentTradeRequest;
import com.titanjr.checkstand.request.TLGatewayPayDownloadRequest;
import com.titanjr.checkstand.request.TLQrCodeDownloadRequest;
import com.titanjr.checkstand.respnse.RSResponse;
import com.titanjr.checkstand.respnse.TLQrCodeDownloadResponse;
import com.titanjr.checkstand.service.AccountDownloadService;
import com.titanjr.checkstand.util.CommonUtil;
import com.titanjr.checkstand.util.MyHostnameVerifier;
import com.titanjr.checkstand.util.MyX509TrustManager;
import com.titanjr.checkstand.util.SignMsgBuilder;
import com.titanjr.checkstand.util.tlUtil.XmlTools;

/**
 * 对账文件下载服务实现
 * @author Jerry
 * @date 2018年3月15日 下午5:17:15  
 */
@Service("accountDownloadService")
public class AccountDownloadServiceImpl implements AccountDownloadService {
	
	private final static Logger logger = LoggerFactory.getLogger(AccountDownloadServiceImpl.class);
	
	private static String resUrl  = System.getProperty("checkstand.root")+File.separator+"WEB-INF"+File.separator;
	private final String tmpUrl = System.getProperty("java.io.tmpdir"); //临时目录
	
	@Resource
	private TitanSysconfigService titanSysconfigService;
	
	@Resource
	private AccountDownloadDao accountDownloadDao;
	

	@Override
	public RSResponse tlGatewayPayDownload(TLGatewayPayDownloadRequest tlGatewayPayDownloadRequest) {

		RSResponse response = new RSResponse();
		response.setMerchantNo(SysConstant.RS_MERCHANT_NO);
		response.setVersion(SysConstant.RS_VERSION);
		response.setSignType(SysConstant.RS_SIGN_TYPE);
		//String responseStr = "";
		FileWriter fwriter = null;  
		
		try{
			
			String configKey = tlGatewayPayDownloadRequest.getMchtCd() +"_" + PayTypeEnum.AGENT_TRADE.combPayType + 
					"_" + SysConstant.TL_CHANNEL_CODE + "_" + tlGatewayPayDownloadRequest.getRequestType();
			GateWayConfigDTO gateWayConfigDTO = SysConstant.gateWayConfigMap.get(configKey);
			if(gateWayConfigDTO == null){
				logger.error("【通联-网关支付对账文件下载】失败，获取网关配置为空，configKey={}", configKey);
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return response;
			}
			tlGatewayPayDownloadRequest.setSignMsg(SignMsgBuilder.getSignMsgForGatewayDoanload(
					tlGatewayPayDownloadRequest, gateWayConfigDTO.getSecretKey()));
			
			String fileAsString = ""; // 签名信息前的对账文件内容
			String fileSignMsg = ""; // 文件签名信息
			@SuppressWarnings("unused")
			boolean isVerified = false; // 验证签名结果
			// 建立连接
			URL url = new URL(gateWayConfigDTO.getGateWayUrl() 
					+ "?mchtCd=" + tlGatewayPayDownloadRequest.getMchtCd() 
					+ "&settleDate=" + tlGatewayPayDownloadRequest.getSettleDate() 
					+ "&signMsg=" + tlGatewayPayDownloadRequest.getSignMsg());
			HttpURLConnection httpConn = this.getHttpsURLConnection(url);
			httpConn.connect();
			// 读取交易结果
			BufferedReader fileReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			StringBuffer fileBuf = new StringBuffer(); 
			// 签名信息前的字符串
			String lines;
			while ((lines = fileReader.readLine()) != null) {
				if (lines.length() > 0) {
					// 按行读，每行都有换行符
					fileBuf.append(lines + "\r\n");
				} else {
					// 文件中读到空行，则读取下一行为签名信息
					fileSignMsg = fileReader.readLine();
				}
			}
			fileReader.close();
			fileAsString = fileBuf.toString();
			if(GatewayDwonloadErrCodeEnum.isExist(fileAsString.replace("\r\n", ""))){
				logger.error("【通联-网关支付对账文件下载】失败：{}", GatewayDwonloadErrCodeEnum.getValue(fileAsString.replace("\r\n", "")));
				response.putErrorResult(RSErrorCodeEnum.build(GatewayDwonloadErrCodeEnum.getValue(fileAsString.replace("\r\n", ""))));
				return response;
			}
			
			//验证签名：先对文件内容计算 MD5 摘要，再将 MD5 摘要作为明文进行签名验证
			String fileMd5 = SecurityUtil.MD5Encode(fileAsString);
			isVerified = SecurityUtil.verifyByRSA(resUrl+SysConstant.MD5_CER_PATH, fileMd5.getBytes(), Base64.decode(fileSignMsg));
			/*if (isVerified) {
				logger.info("【通联-网关支付对账文件下载】签名验证通过，上传并保存文件数据...");*/
				String fileName = tlGatewayPayDownloadRequest.getSettleDate()+".txt";
				File accountLocal = new File(tmpUrl+SysConstant.TL_GATEWAY_DIR+"/");
				accountLocal.mkdir();
				
				fwriter = new FileWriter(accountLocal.getPath()+"\\"+fileName);  
				fwriter.write(fileAsString);
				fwriter.flush();
				fwriter.close();
				logger.info("文件临时保存为："+accountLocal.getPath()+"\\"+fileName);
				
				//对账文件信息保存到数据库
				this.saveTLGatewayDowanloadInfo(fileAsString);
				//对账文件上传到房仓FTP
				this.uploadFtp(accountLocal.getPath()+"\\"+fileName, fileName, SysConstant.TL_GATEWAY_DIR);
				
			/*} else {
				logger.error("【通联-网关支付对账文件下载】签名验证失败，丢弃该文件，日期：{}", tlGatewayPayDownloadRequest.getSettleDate());
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			}*/
			initRSResponse(response);
			return response;
			
			//---------------------------下面的写法也可以，上面是通联的写法
			/*//构建参数，发送请求
			HttpPost httpPost = new HttpPost(gateWayConfigDTO.getGateWayUrl());
			List<NameValuePair> params = BeanConvertor.beanToList(tlGatewayPayDownloadRequest);
			logger.info("【通联-网关支付对账文件下载】请求参数：{}", tlGatewayPayDownloadRequest.toString());
			
			//发送请求
			HttpResponse httpRes = HttpClient.httpRequest(params, httpPost);
			if (null != httpRes) {
				
				HttpEntity entity = httpRes.getEntity();
				responseStr = EntityUtils.toString(entity, "UTF-8");
				logger.info("【通联-网关支付对账文件下载】返回信息：{}", responseStr);
				// 读取交易结果
				BufferedReader fileReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
						responseStr.getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8")));
				StringBuffer fileBuf = new StringBuffer(); 
				// 签名信息前的字符串
				String lines;
				while ((lines = fileReader.readLine()) != null) {
					if (lines.length() > 0) {
						// 按行读，每行都有换行符
						fileBuf.append(lines + "\r\n");
					} else {
						// 文件中读到空行，则读取下一行为签名信息
						fileSignMsg = fileReader.readLine();
					}
				}
				fileReader.close();
				fileAsString = fileBuf.toString();
				
				// 验证签名：先对文件内容计算 MD5 摘要，再将 MD5 摘要作为明文进行签名验证
				String fileMd5 = SecurityUtil.MD5Encode(fileAsString);
				isVerified = SecurityUtil.verifyByRSA(resUrl+SysConstant.CER_PATH, fileMd5.getBytes(), Base64.decode(fileSignMsg));
				if (isVerified) {
					logger.info("【通联-网关支付对账文件下载】签名验证通过");
					
				} else {
					logger.error("【通联-网关支付对账文件下载】签名验证失败，丢弃该文件，日期：{}", tlGatewayPayDownloadRequest.getSettleDate());
				}
				
				return response;
				
			}else{
				
				logger.error("【通联-网关支付对账文件下载】失败 httpRes为空");
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return response;
			}*/
			
		}catch(Exception e){
			
			logger.error("【通联-网关支付对账文件下载】异常：", e);
			response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return response;
			
		}/*finally {
			try {
				fwriter.flush();
				fwriter.close();
			} catch (IOException e) {
				logger.error("【通联-网关支付对账文件下载】异常：", e);
			}
		}*/
		
	}
	
	
	@SuppressWarnings({ "unused", "resource" })
	@Override
	public RSResponse tlQrCodePayDownload(TLQrCodeDownloadRequest tlQrCodeDownloadRequest) {

		TLQrCodeDownloadResponse tlQrCodeDownloadResponse = new TLQrCodeDownloadResponse();
		RSResponse response = new RSResponse();
		response.setMerchantNo(SysConstant.RS_MERCHANT_NO);
		response.setVersion(SysConstant.RS_VERSION);
		response.setSignType(SysConstant.RS_SIGN_TYPE);
		String responseStr = "";
		
		try {
			
			//获取网关配置
			String configKey = tlQrCodeDownloadRequest.getCusid() +"_" + PayTypeEnum.QR_WECHAT_URL.combPayType + 
					"_" + SysConstant.TL_CHANNEL_CODE + "_" + tlQrCodeDownloadRequest.getRequestType();
			GateWayConfigDTO gateWayConfigDTO = SysConstant.gateWayConfigMap.get(configKey);
			if(gateWayConfigDTO == null){
				logger.error("【通联-扫码/公众号支付对账文件下载】失败，获取网关配置为空，configKey={}", configKey);
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return response;
			}
			
			//构建参数，发送请求
			String params = buildRequestParam(tlQrCodeDownloadRequest, gateWayConfigDTO.getAppId(), 
					gateWayConfigDTO.getSecretKey());
			logger.info("【通联-扫码/公众号支付对账文件下载】请求参数：{}", CommonUtil.toLineString(params));
			HttpPost httpPost = new HttpPost(gateWayConfigDTO.getGateWayUrl() + "?" + params.toString());
			HttpResponse httpRes = HttpClient.httpRequest(new ArrayList<NameValuePair>(), httpPost);
			
			if (null != httpRes) {
				HttpEntity entity = httpRes.getEntity();
				responseStr = EntityUtils.toString(entity, "UTF-8");
				logger.info("【通联-扫码/公众号支付对账文件下载】返回信息：\n{}", responseStr);
				
				tlQrCodeDownloadResponse = (TLQrCodeDownloadResponse)JsonUtil.jsonToBean(responseStr, TLQrCodeDownloadResponse.class);
				if(!"SUCCESS".equals(tlQrCodeDownloadResponse.getRetcode()) 
						|| !StringUtil.isValidString(tlQrCodeDownloadResponse.getUrl())){
					logger.error("【通联-扫码/公众号支付对账文件下载】获取zip文件地址失败");
					
				}else {
					
					logger.info("【通联-扫码/公众号支付对账文件下载】获取zip文件地址成功，开始保存...");
					String fileName = DateUtil.dateToString(DateUtil.stringToDate(tlQrCodeDownloadRequest.
							getDate(), "yyyyMMdd"), "yyyy-MM-dd");
					//File localZipPath = new File(tmpUrl+SysConstant.TL_QECODE_DIR+"/");
					File localZipPath = new File("F:/test/");
					localZipPath.mkdir();
					int byteread = 0;
					int bytesum = 0;
					InputStream inStream=null;
					FileOutputStream fs =null;
					URL url = new URL(tlQrCodeDownloadResponse.getUrl());
					URLConnection conn = url.openConnection();
					inStream = conn.getInputStream();
					fs = new FileOutputStream(localZipPath.getPath()+"\\"+fileName+".zip");
					byte[] buffer = new byte[4028];
					while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
					}
					logger.info("【通联-扫码/公众号支付对账文件下载】保存成功开始解压，地址：{}", localZipPath.getPath()+"\\"+fileName+".zip");
					//解压
					ZipInputStream zin=new ZipInputStream(new FileInputStream(new File(localZipPath.getPath()+"\\"+fileName+".zip")));
					while (zin.getNextEntry() != null) {
						 FileOutputStream os = new FileOutputStream(localZipPath.getPath()+"\\"+fileName+".xlsx");  
			             // Transfer bytes from the ZIP file to the output file  
			             byte[] buf = new byte[1024];  
			             int len;  
			             while ((len = zin.read(buf)) > 0) {  
			                 os.write(buf, 0, len);  
			             }  
			             os.close();  
			             zin.closeEntry();
					}
					//对账文件信息保存到数据库
					this.saveTLQrCodeDowanloadInfo(localZipPath.getPath()+"\\"+fileName+".xlsx");
					//对账文件上传到房仓FTP
					uploadFtp(localZipPath.getPath()+"\\"+fileName+".xlsx", fileName+".xlsx", SysConstant.TL_QECODE_DIR);
					
				}
				initRSResponse(response);
				return response;
				
			}else{
				
				logger.error("【通联-扫码/公众号支付对账文件下载】失败 httpRes为空");
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);;
				return response;
			}
			
		} catch (Exception e) {
			logger.error("【通联-扫码/公众号支付对账文件下载】发生异常：", e);
			response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return response;
		}

	}
	
	
	@Override
	public RSResponse tlAgentDownload(TLAgentTradeRequest tlAgentTradeRequest) {
		
		RSResponse response = new RSResponse();
		
		try{
			
			TLAgentQueryTransDTO trans = (TLAgentQueryTransDTO)tlAgentTradeRequest.getTrxData().get(0);
			String configKey = trans.getMERCHANT_ID() +"_" + PayTypeEnum.AGENT_TRADE.combPayType + 
					"_" + SysConstant.TL_CHANNEL_CODE + "_" + tlAgentTradeRequest.getRequestType();
			GateWayConfigDTO gateWayConfigDTO = SysConstant.gateWayConfigMap.get(configKey);
			if(gateWayConfigDTO == null){
				logger.error("【通联-对账文件下载】失败，获取网关配置为空，configKey={}，orderNo={}", configKey, 
						tlAgentTradeRequest.getINFO().getREQ_SN());
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return response;
			}
			
			String resp = sendXml(tlAgentTradeRequest, gateWayConfigDTO.getGateWayUrl());
			writeBill(resp);
			
			initRSResponse(response);
			return response;
			
		}catch(Exception e){
			
			if(e.getCause() instanceof ConnectException||e instanceof ConnectException){
				logger.error("【通联-对账文件下载】请求链接中断");
			}
			logger.error("【通联-对账文件下载】异常：", e);
			response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return response;
		}
		
	}
	
	
	@Override
	public RSResponse rbAccountDownload(RBAgentDownloadRequest rbAgentDownloadRequest) {

		RSResponse response = new RSResponse();

		try {
			String configKey = rbAgentDownloadRequest.getMerchant_id() + "_" + PayTypeEnum.AGENT_TRADE.combPayType +
					"_" + SysConstant.RB_CHANNEL_CODE + "_" + rbAgentDownloadRequest.getRequestType();
			GateWayConfigDTO gateWayConfigDTO = SysConstant.gateWayConfigMap.get(configKey);
			if (gateWayConfigDTO == null) {
				logger.error("【融宝-对账文件下载】失败，获取网关配置为空，configKey={}", configKey);
				response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
				return response;
			}

			String transferFormat = DateUtil.dateToString(DateUtil.stringToDate(rbAgentDownloadRequest.
					getTradeDate(), "yyyyMMdd"), "yyyy-MM-dd");

			String accountFileName = rbAgentDownloadRequest.getTradeDate() + ".txt";
			File accountLocal = new File(tmpUrl + SysConstant.RB_ACCOUNT_DIR);
			accountLocal.mkdir();
			String rechargeFileName = transferFormat + ".txt";
			File rechargeLocal = new File(tmpUrl + SysConstant.RB_RECHARGE_DIR);
			rechargeLocal.mkdir();
			String refundFileName = transferFormat + ".txt";
			File refundLocal = new File(tmpUrl + SysConstant.RB_REFUND_DIR);
			refundLocal.mkdir();

			//登录融宝ftp下载文件
			FtpUtil util = new FtpUtil();
			util.loginRemote(SysConstant.RB_FTP_HOST, SysConstant.RB_FTP_USER, SysConstant.RB_FTP_PWD);
			logger.info("login rbFtp success");
			util.downloadFile(accountFileName, accountLocal.getPath(), SysConstant.RB_ACCOUNT_DIR);
			util.downloadFile(rechargeFileName, rechargeLocal.getPath(), SysConstant.RB_RECHARGE_DIR + transferFormat);
			util.downloadFile(refundFileName,refundLocal.getPath() , SysConstant.RB_REFUND_DIR + transferFormat);
			util.ftpLogOut();
			
			//保存到数据库
			saveRBAccountDowanloadInfo(rechargeLocal.getPath()+"/"+rechargeFileName, RBTradeTypeEnum.RB_PAY.tradeType);
			saveRBAccountDowanloadInfo(refundLocal.getPath()+"/"+refundFileName, RBTradeTypeEnum.RB_REFUND.tradeType);
			saveRBAccountDowanloadInfo(accountLocal.getPath()+"/"+accountFileName, RBTradeTypeEnum.RB_AGENT.tradeType);

			//登录房仓ftp并上传
			FTPConfigResponse configResponse = titanSysconfigService.getFTPConfig();
			util = new FtpUtil(configResponse.getFtpServerIp(),
					configResponse.getFtpServerPort(),
					configResponse.getFtpServerUser(),
					configResponse.getFtpServerPassword());
			util.ftpLogin();
			logger.info("login fcFtp success");

			FTPFileUpload(util, "ACCOUNT-", SysConstant.RB_ACCOUNT_DIR, accountFileName);
			FTPFileUpload(util, "RECHARGE-", SysConstant.RB_RECHARGE_DIR, rechargeFileName);
			FTPFileUpload(util, "REFUND-", SysConstant.RB_REFUND_DIR, refundFileName);

			util.ftpLogOut();
			logger.info("对账文件下载并上传成功");

			initRSResponse(response);
			return response;

		} catch (Exception e) {
			logger.error("【融宝-对账文件下载】发生异常：", e);
			response.putErrorResult(RSErrorCodeEnum.SYSTEM_ERROR);
			return response;
		}

	}
	
	
	/**
	 * 得到 HttpURLConnection 对象
	 * @author Jerry
	 * @date 2018年3月14日 下午3:45:34
	 */
	public HttpURLConnection getHttpsURLConnection(URL url) {
		try {
			HttpURLConnection httpConnection = (HttpURLConnection) 
			url.openConnection();
			if ("https".equals(url.getProtocol())) {// 如果是 https 协议
				HttpsURLConnection httpsConn = (HttpsURLConnection) 
				httpConnection;
				TrustManager[] managers = { new MyX509TrustManager() };// 证书过滤
				SSLContext sslContext;
				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, managers, new SecureRandom());
				
				SSLSocketFactory ssf = sslContext.getSocketFactory();
				httpsConn.setSSLSocketFactory(ssf);
				httpsConn.setHostnameVerifier(new MyHostnameVerifier());
				//主机名过滤
				return httpsConn;
			}
			return httpConnection;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	
	private String buildRequestParam(TLQrCodeDownloadRequest tlQrCodeDownloadRequest, String appId, 
			String secretKey) throws UnsupportedEncodingException{
		
		//TreeMap会按字段名的ASCLL码从小到大排序
		TreeMap<String,String> params = new TreeMap<String,String>();
		params.put("cusid", tlQrCodeDownloadRequest.getCusid());
		params.put("appid", appId);
		params.put("date", tlQrCodeDownloadRequest.getDate());
		params.put("randomstr", tlQrCodeDownloadRequest.getRandomstr());
		params.put("key", secretKey);
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, String> entry:params.entrySet()){
			if(entry.getValue()!=null&&entry.getValue().length()>0){
				sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		logger.info("【通联-扫码/公众号对账文件下载】加密前排序为：{}", sb.toString());
		String md5Msg = MD5.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
		logger.info("【通联-扫码/公众号对账文件下载】加密后sign为：{}", md5Msg);
		params.put("sign", md5Msg);
		params.remove("key");
		
		StringBuilder paramBuf = new StringBuilder();
    	boolean isNotFirst = false;
    	for (Map.Entry<String, String> entry: params.entrySet()){
    		if(entry.getValue()!=null&&entry.getValue().length()>0){
	    		if (isNotFirst)
	    			paramBuf.append('&');
	    		isNotFirst = true;
	    		paramBuf
	    			.append(entry.getKey())
	    			.append('=')
	    			.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
    		}
    	}
    	
    	logger.info("【通联-扫码/公众号对账文件下载】上送参数：{}", paramBuf.toString());
		return paramBuf.toString();
		
	}
	
	
	private boolean saveTLGatewayDowanloadInfo(String fileString){
		
		try {
			
			List<AccountDownloadDTO> list = new ArrayList<AccountDownloadDTO>();
			String[] array = fileString.split("\r\n");
			
			for (int i = 1; i < array.length; i++) {
				
				String[] strs = array[i].split("\\|");
				AccountDownloadDTO accountDownloadDTO = new AccountDownloadDTO();
				//商户号
				accountDownloadDTO.setMerchantNo(strs[2]);
				//商户订单号
				accountDownloadDTO.setOrderNo(strs[4]);
				//第三方订单号
				accountDownloadDTO.setPartnerOrderNo(strs[5]);
				//第三方编码【01通联 02融宝】
				accountDownloadDTO.setChannelCode(SysConstant.TL_CHANNEL_CODE);
				//交易时间    yyyy-MM-dd HH:mm:ss
				accountDownloadDTO.setTradeDate(strs[3]);
				//结算日期    yyyy-MM-dd
				accountDownloadDTO.setSettlDate(strs[1]);
				//交易类型  1充值  2退款  3冲销   4提现  0未知类型
				String tradeTypeDes = strs[0];
				accountDownloadDTO.setTradeTypeDes(tradeTypeDes);
				accountDownloadDTO.setTradeType(TLTradeTypeEnum.getTradeType(tradeTypeDes));
				//交易金额
				accountDownloadDTO.setTradeAmount(strs[6]);
				//清算金额
				accountDownloadDTO.setSettlAmount(strs[8]);
				//手续费
				accountDownloadDTO.setFee(strs[7]);
				
				list.add(accountDownloadDTO);
				
			}
			
			int saveCount = accountDownloadDao.batchSave(list);
			if(saveCount > 0){
				logger.info("【通联-网关支付】对账文件信息保存成功，共保存{}条记录", saveCount);
				return true;
			}
			return false;
			
		} catch (Exception e) {
			
			logger.error("【通联-网关支付】对账信息保存到数据库异常：", e);
       	 	return false;
			
		}
		
	}
	
	

	
	
	/**
	 * poi读取xlsx保存到数据库
	 * @author Jerry
	 * @date 2018年3月16日 下午3:39:41
	 */
	private boolean saveTLQrCodeDowanloadInfo(String filePath){
		
		List<AccountDownloadDTO> list = new ArrayList<AccountDownloadDTO>();
		String merchantNo = "";
		String tradeDate = "";
		String channelCode = SysConstant.TL_CHANNEL_CODE;
        try{
        	
            File file = new File(filePath);
            InputStream str = new FileInputStream(file);
            XSSFWorkbook xwb = new XSSFWorkbook(str);  //利用poi读取excel文件流
            XSSFSheet st = xwb.getSheetAt(0);  //读取sheet的第一个工作表
            int rows = st.getLastRowNum();//总行数，好像会少一行，所以下面遍历是rows-2
            logger.info("总行数："+rows);
            //读取商户号
            XSSFRow row = st.getRow(2);
            if(row!=null){
            	XSSFCell cell = row.getCell(7);
            	if(cell != null){
            		merchantNo = cell.getStringCellValue();
            	}
            }
            //遍历对账明细，提取信息（如果通联的excel对账文件格式有变化，数据会不准）
            for (int i = 10; i < rows-2; i++) {
            	XSSFCell cell = null;
            	AccountDownloadDTO accountDownloadDTO = new AccountDownloadDTO();
            	accountDownloadDTO.setMerchantNo(merchantNo);
            	accountDownloadDTO.setChannelCode(channelCode);
            	row = st.getRow(i);
            	//交易时间
            	cell = row.getCell(11);
            	tradeDate = cell.getStringCellValue();
            	cell = row.getCell(1);
            	accountDownloadDTO.setTradeDate(tradeDate+" "+cell.getStringCellValue());
            	//商户订单号
            	cell = row.getCell(13);
            	accountDownloadDTO.setOrderNo(cell.getStringCellValue());
            	//第三方订单号
            	cell = row.getCell(12);
            	accountDownloadDTO.setPartnerOrderNo(cell.getStringCellValue());
            	//交易类型  1充值  2退款  3冲销   4提现  0未知类型
            	cell = row.getCell(2);
            	String tradeTypeDes = cell.getStringCellValue();
            	accountDownloadDTO.setTradeTypeDes(tradeTypeDes);
            	accountDownloadDTO.setTradeType(TLTradeTypeEnum.getTradeType(tradeTypeDes));
            	//交易金额
            	cell = row.getCell(9);
            	accountDownloadDTO.setTradeAmount(cell.getStringCellValue());
            	//手续费
            	cell = row.getCell(10);
            	accountDownloadDTO.setFee(cell.getStringCellValue());
            	
            	list.add(accountDownloadDTO);
			}
            
            int saveCount = accountDownloadDao.batchSave(list);
            if(saveCount > 0){
            	logger.info("【通联-扫码/公众号】对账文件信息保存成功，共保存{}条记录", saveCount);
            	return true;
            }
            return false;
            
        }catch(IOException e){
        	
        	 logger.error("【通联-扫码/公众号】操作excel对账信息保存到数据库异常：", e);
        	 return false;
        	
        }
         
    }
	
	
	/**
	 * 通联账户交易对账文件信息保存到数据库
	 * @author Jerry
	 * @date 2018年3月27日 下午2:42:19
	 */
	private boolean saveTLAgentDowanloadInfo(String filePath){
		
		try {
			
			List<AccountDownloadDTO> list = new ArrayList<AccountDownloadDTO>();
			StringBuffer fileBuf = new StringBuffer();
			String fileString = "";
			//读取对账文件
			File file = new File(filePath);
			if (file.isFile()) { 
	            //使用Reader，表示读字符数据，使用BufferedReader，表示建立缓冲区读字符数据。
	            BufferedReader bufferedReader = null; 
	            FileReader fileReader = null; 
	            try { 
	                fileReader = new FileReader(file);
	                //嵌套使用，装饰者模式
	                bufferedReader = new BufferedReader(fileReader); 
	                String line = bufferedReader.readLine(); 
	                //一行一行读
	                while (line != null) {
	                	fileBuf.append(line + "\r\n");
	                    line = bufferedReader.readLine(); 
	                } 
	            } catch (Exception e) {
	            	logger.error("【通联】账户交易对账文件信息保存数据库失败，读取文件异常：", e);
	            	return false;
	            	
	            }  finally {
	                try { 
	                    fileReader.close(); 
	                    bufferedReader.close(); 
	                } catch (IOException e) { 
	                    e.printStackTrace(); 
	                }
	            } 
	   
	        }
			
			//解析文件内容，构建对象
			fileString = fileBuf.toString();
			String[] array = fileString.split("\r\n");
			for (int i = 1; i < array.length; i++) {
				
				String[] strs = array[i].split(" ");
				AccountDownloadDTO accountDownloadDTO = new AccountDownloadDTO();
				//商户号
				accountDownloadDTO.setMerchantNo(SysConstant.TL_AGENT_MERCHANT);
				//商户订单号
				accountDownloadDTO.setOrderNo(strs[0]);
				//第三方编码【01通联 02融宝】
				accountDownloadDTO.setChannelCode(SysConstant.TL_CHANNEL_CODE);
				//交易时间    yyyy-MM-dd HH:mm:ss
				StringBuffer tradeDateSB = new StringBuffer(strs[6]);
				accountDownloadDTO.setTradeDate(tradeDateSB.insert(4, "-").insert(7, "-").insert(10, " ")
						.insert(13, ":").insert(16, ":").toString());
				//结算日期    yyyy-MM-dd
				StringBuffer settlDateSB = new StringBuffer(strs[7]);
				accountDownloadDTO.setSettlDate(settlDateSB.insert(4, "-").insert(7, "-").toString());
				//交易类型  1充值  2退款  3冲销   4提现  0未知类型
				TLTradeTypeEnum tlTradeTypeEnum = TLTradeTypeEnum.getByKey(strs[2]);
				accountDownloadDTO.setTradeTypeDes(tlTradeTypeEnum.value);
				accountDownloadDTO.setTradeType(tlTradeTypeEnum.tradeType);
				//交易金额
				accountDownloadDTO.setTradeAmount(strs[4]);
				//手续费可能没有，需要判断
				if(strs.length == 9){
					accountDownloadDTO.setFee(StringUtil.isValidString(strs[8])?strs[8]:"0");
				}
				if(strs.length == 10){
					accountDownloadDTO.setFee(StringUtil.isValidString(strs[9])?strs[9]:"0");
				}
				
				list.add(accountDownloadDTO);
				
			}
			
			int saveCount = accountDownloadDao.batchSave(list);
			if(saveCount > 0){
				logger.info("【通联-账户交易】对账文件信息保存成功，共保存{}条记录", saveCount);
				return true;
			}
			return false;
			
		} catch (Exception e) {
			
			logger.error("【通联-账户交易】对账信息保存到数据库异常：", e);
       	 	return false;
			
		}
		
	}
	
	
	private boolean saveRBAccountDowanloadInfo(String filePath, String tradeType){
		
		try {
			
			List<AccountDownloadDTO> list = new ArrayList<AccountDownloadDTO>();
			StringBuffer fileBuf = new StringBuffer();
			String fileString = "";
			//读取对账文件
			File file = new File(filePath);
			if (file.isFile()) { 
	            //使用Reader，表示读字符数据，使用BufferedReader，表示建立缓冲区读字符数据。
	            BufferedReader bufferedReader = null; 
	            FileReader fileReader = null; 
	            try { 
	                fileReader = new FileReader(file);
	                //嵌套使用，装饰者模式
	                bufferedReader = new BufferedReader(fileReader); 
	                String line = bufferedReader.readLine(); 
	                //一行一行读
	                while (line != null) {
	                	fileBuf.append(line + "\r\n");
	                    line = bufferedReader.readLine(); 
	                } 
	            } catch (Exception e) {
	            	logger.error("【融宝】对账文件信息保存数据库失败，读取文件异常：", e);
	            	return false;
	            	
	            }  finally {
	                try { 
	                    fileReader.close(); 
	                    bufferedReader.close(); 
	                } catch (IOException e) { 
	                    e.printStackTrace(); 
	                }
	            } 
	   
	        }
			
			//解析文件内容，构建对象
			fileString = fileBuf.toString();
			String[] array = fileString.split("\r\n");
			for (int i = 1; i < array.length; i++) {
				
				String[] strs = array[i].split("\\|");
				AccountDownloadDTO accountDownloadDTO = new AccountDownloadDTO();
				//商户号
				accountDownloadDTO.setMerchantNo(SysConstant.TL_AGENT_MERCHANT);
				//第三方编码【01通联 02融宝】
				accountDownloadDTO.setChannelCode(SysConstant.RB_CHANNEL_CODE);
				//交易类型  1充值  2退款  3冲销   4提现  0未知类型
				accountDownloadDTO.setTradeType(tradeType);
				accountDownloadDTO.setTradeTypeDes(RBTradeTypeEnum.getValueByTradeType(tradeType));
				
				if(!tradeType.equals(RBTradeTypeEnum.RB_AGENT.tradeType)){
					
					//商户订单号
					accountDownloadDTO.setOrderNo(strs[0]);
					//第三方订单号
					accountDownloadDTO.setPartnerOrderNo(strs[1]);
					//卡类型  融宝充值和退款文件中  0：借记卡    1：贷记卡
					if("0".equals(strs[5])){
						accountDownloadDTO.setCardType("1");
					}else{
						accountDownloadDTO.setCardType("2");
					}
					
					if(tradeType.equals(RBTradeTypeEnum.RB_PAY.tradeType)){//充值
						//交易时间    yyyy-MM-dd HH:mm:ss
						accountDownloadDTO.setTradeDate(strs[13]);
						//交易状态
						accountDownloadDTO.setTradeStatus("1");//入金对账文件只有交易成功的记录
						//交易金额
						accountDownloadDTO.setTradeAmount(strs[6]);
						//清算金额
						accountDownloadDTO.setSettlAmount(strs[10]);
						//手续费
						accountDownloadDTO.setFee(strs[9]);
						//付款方
						accountDownloadDTO.setPayer(strs[14]);
						//收款方
						accountDownloadDTO.setPayee(strs[15]);
					}
					if(tradeType.equals(RBTradeTypeEnum.RB_REFUND.tradeType)){//退款
						//交易时间    yyyy-MM-dd HH:mm:ss
						accountDownloadDTO.setTradeDate(strs[10]);
						//交易状态
						if(StringUtil.isValidString(strs[9])){
							if("processing".equals(strs[9])){
								accountDownloadDTO.setTradeStatus("2");
							}else if("failed".equals(strs[9])){
								accountDownloadDTO.setTradeStatus("2");
							}else{
								accountDownloadDTO.setTradeStatus("1");
							}
						}
						//交易金额
						accountDownloadDTO.setTradeAmount(strs[7]);
						//付款方
						accountDownloadDTO.setPayer(strs[12]);
						//收款方
						accountDownloadDTO.setPayee(strs[13]);
					}
					
				}else{  //账户交易
					
					//第三方订单号
					accountDownloadDTO.setPartnerOrderNo(strs[0]);
					//交易时间    yyyy-MM-dd HH:mm:ss
					accountDownloadDTO.setTradeDate(strs[12]);
					//交易状态
					if("0".equals(strs[13])){
						accountDownloadDTO.setTradeStatus("1");
					}else{
						accountDownloadDTO.setTradeStatus("2");
					}
					//交易金额
					accountDownloadDTO.setTradeAmount(strs[8]);
					//手续费
					accountDownloadDTO.setFee(strs[9]);
					//银行账户
					accountDownloadDTO.setTradeAmount(strs[3]);
					//卡类型
					accountDownloadDTO.setCardType(strs[6]); //???
					
				}
				
				/*accountDownloadDTO.setTradeDate(tradeDateSB.insert(4, "-").insert(7, "-").insert(10, " ")
						.insert(13, ":").insert(16, ":").toString());*/
				list.add(accountDownloadDTO);
				
			}
			
			if(CollectionUtils.isNotEmpty(list)){
				int saveCount = accountDownloadDao.batchSave(list);
				if(saveCount > 0){
					logger.info("【通联-账户交易】对账文件信息保存成功，共保存{}条记录", saveCount);
					return true;
				}
				return false;
			}else{
				logger.info("【通联-账户交易】没有需要插入的交易记录，交易类型为：{}", RBTradeTypeEnum.getValueByTradeType(tradeType));
				return false;
			}
			
		} catch (Exception e) {
			
			logger.error("【通联-账户交易】对账信息保存到数据库异常：", e);
       	 	return false;
			
		}
		
	}
	
	
	/**
	 * 对账文件上传到ftp并保存到数据库
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
		String fileName = DateUtil.dateToString(new Date(), "yyyy-MM-dd")+".txt";
		
		File agentLocal = new File(tmpUrl+SysConstant.TL_AGENT_DIR+"/");
		agentLocal.mkdir();
		
		//先写入压缩文件到本地
		FileOutputStream sos=null;
		sos=new FileOutputStream(new File(agentLocal.getPath()+"bill.gz"));
		Base64InputStream b64is=new Base64InputStream(IOUtils.toInputStream(billContext),false);
		IOUtils.copy(b64is, sos);
		IOUtils.closeQuietly(b64is);
		//解压
		ZipInputStream zin=new ZipInputStream(new FileInputStream(new File(agentLocal.getPath()+"bill.gz")));
		while (zin.getNextEntry() != null) {
			 FileOutputStream os = new FileOutputStream(agentLocal.getPath()+fileName);  
             // Transfer bytes from the ZIP file to the output file  
             byte[] buf = new byte[1024];  
             int len;  
             while ((len = zin.read(buf)) > 0) {  
                 os.write(buf, 0, len);  
             }  
             os.close();  
             zin.closeEntry();
		}
		
		this.saveTLAgentDowanloadInfo(agentLocal.getPath()+fileName);
		this.uploadFtp(agentLocal.getPath()+fileName, fileName, SysConstant.TL_AGENT_DIR);
		
	}
	
	
	/**
	 * 发送报文
	 * @author Jerry
	 * @date 2017年12月28日 上午11:29:49
	 */
	private String sendXml(TLAgentTradeRequest tlAgentTradeRequest, String url) throws Exception{
		
		logger.info("resUrl：{}", resUrl);
		//logger.info("resUrl_pro：{}", resUrl_pro);
		
		String xml = XmlTools.buildXml(tlAgentTradeRequest, true);
		xml = XmlTools.signMsg(xml, resUrl+SysConstant.PFX_PATH, SysConstant.PFX_PWD, false);
		
		logger.info("======================发送报文======================：\n{}", xml);
		String resp = XmlTools.send(url, xml);
		logger.info("======================响应内容======================") ;
		
		boolean flag = XmlTools.verifySign(resp, resUrl+SysConstant.CER_PATH, false, false);
		logger.info("验签结果[{}]", flag) ;
		
		return resp;
		
	}
	
	
	/**
	 * 上传房仓FTP
	 * @author Jerry
	 * @date 2018年3月22日 下午4:50:36
	 */
	private void uploadFtp(String filePath, String fileName, String businessDir) throws Exception{
		
		//登录ftp并上传文件
		FtpUtil util = null;
		FTPConfigResponse configResponse = titanSysconfigService.getFTPConfig();
		util = new FtpUtil(configResponse.getFtpServerIp(),
				configResponse.getFtpServerPort(),
				configResponse.getFtpServerUser(),
				configResponse.getFtpServerPassword());
		util.ftpLogin();
		logger.info("login ftp success");
		File file = new File(filePath);
		InputStream inputStream = new FileInputStream(file);
		util.uploadStream(fileName, inputStream, FtpUtil.UPLOAD_PATH_TL_AGENT_CHECKING+businessDir);
		logger.info("upload to ftp success fileName=" + fileName);
		util.ftpLogOut();
		
		logger.info("网关支付对账文件下载并上传成功");
		
	}


	private boolean FTPFileUpload(FtpUtil util, String prefix, String baseDir, String fileName) {
		File file = new File(tmpUrl + baseDir + fileName);
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			if("ACCOUNT-".equals(prefix)){
				StringBuffer sb = new StringBuffer(fileName);
				sb.insert(4, "-").insert(7, "-");
				fileName = sb.toString();
			}
			util.uploadStream(prefix + fileName, inputStream, FtpUtil.UPLOAD_PATH_RB_AGENT_CHECKING);
		} catch (FileNotFoundException e) {
			logger.error("本地文件流读取失败,本地文件路径：{}", file.getPath(), e);
			return false;
		} catch (Exception e) {
			logger.error("FTP上传失败", e);
			return false;
		}

		logger.info("upload to fcFtp success fileName=" + baseDir + fileName);
		return true;
	}
	
	
	private void initRSResponse(RSResponse response){
		response.setMerchantNo(SysConstant.RS_MERCHANT_NO);
		response.setVersion(SysConstant.RS_VERSION);
		response.setSignType(SysConstant.RS_SIGN_TYPE);
	}

}