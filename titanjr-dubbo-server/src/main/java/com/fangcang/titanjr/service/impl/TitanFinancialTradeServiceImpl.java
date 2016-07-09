package com.fangcang.titanjr.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.fangcang.order.api.HotelOrderSearchFacade;
import com.fangcang.order.dto.OrderDetailResponseDTO;
import com.fangcang.titanjr.common.util.*;
import com.fangcang.titanjr.dto.request.*;
import com.fangcang.titanjr.dto.response.*;

import net.sf.json.JSONSerializer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fangcang.corenut.dao.PaginationSupport;
import com.fangcang.finance.enums.TradeStatusEnum;
import com.fangcang.finance.remote.FinanceOrderRemote;
import com.fangcang.finance.remote.FinanceSearchRemote;
import com.fangcang.finance.request.FinanceOrderConfirmRequest;
import com.fangcang.finance.request.FinanceOrderQuery;
import com.fangcang.finance.response.FinanceOrderResponse;
import com.fangcang.titanjr.common.enums.CashierDeskTypeEnum;
import com.fangcang.titanjr.common.enums.EscrowedEnum;
import com.fangcang.titanjr.common.enums.OrderExceptionEnum;
import com.fangcang.titanjr.common.enums.OrderStatusEnum;
import com.fangcang.titanjr.common.enums.ROPErrorEnum;
import com.fangcang.titanjr.common.enums.ReqstatusEnum;
import com.fangcang.titanjr.common.enums.TransferReqEnum;
import com.fangcang.titanjr.common.factory.HessianProxyBeanFactory;
import com.fangcang.titanjr.common.factory.ProxyFactoryConstants;
import com.fangcang.titanjr.common.util.RequestValidationUtil;
import com.fangcang.titanjr.dao.TitanAccountDao;
import com.fangcang.titanjr.dao.TitanDynamicKeyDao;
import com.fangcang.titanjr.dao.TitanOrderPayreqDao;
import com.fangcang.titanjr.dao.TitanPayMethodConfigDao;
import com.fangcang.titanjr.dao.TitanTransOrderDao;
import com.fangcang.titanjr.dao.TitanTransferReqDao;
import com.fangcang.titanjr.dto.bean.AmtTypeEnum;
import com.fangcang.titanjr.dto.bean.CashierItemBankDTO;
import com.fangcang.titanjr.dto.bean.GDPOrderDTO;
import com.fangcang.titanjr.dto.bean.OperTypeEnum;
import com.fangcang.titanjr.dto.bean.OrderExceptionDTO;
import com.fangcang.titanjr.dto.bean.OrderOperateInfoDTO;
import com.fangcang.titanjr.dto.bean.OrderTypeEnum;
import com.fangcang.titanjr.dto.bean.OrgBindInfo;
import com.fangcang.titanjr.dto.bean.PayMethodConfigDTO;
import com.fangcang.titanjr.dto.bean.RechargeDataDTO;
import com.fangcang.titanjr.dto.bean.TitanOrderPayDTO;
import com.fangcang.titanjr.dto.bean.TitanTransferDTO;
import com.fangcang.titanjr.dto.bean.TitanWithDrawDTO;
import com.fangcang.titanjr.dto.bean.TransOrderDTO;
import com.fangcang.titanjr.entity.TitanDynamicKey;
import com.fangcang.titanjr.entity.TitanOrderPayreq;
import com.fangcang.titanjr.entity.TitanPayMethodConfig;
import com.fangcang.titanjr.entity.TitanTransOrder;
import com.fangcang.titanjr.entity.TitanTransferReq;
import com.fangcang.titanjr.entity.parameter.TitanAccountParam;
import com.fangcang.titanjr.entity.parameter.TitanOrderPayreqParam;
import com.fangcang.titanjr.entity.parameter.TitanTransOrderParam;
import com.fangcang.titanjr.entity.parameter.TitanTransferReqParam;
import com.fangcang.titanjr.rs.manager.RSAccTradeManager;
import com.fangcang.titanjr.rs.manager.RSPayOrderManager;
import com.fangcang.titanjr.rs.request.AccountTransferRequest;
import com.fangcang.titanjr.rs.request.OrderOperateRequest;
import com.fangcang.titanjr.rs.request.OrderTransferFlowRequest;
import com.fangcang.titanjr.rs.request.RSPayOrderRequest;
import com.fangcang.titanjr.rs.response.AccountTransferResponse;
import com.fangcang.titanjr.rs.response.OrderOperateInfo;
import com.fangcang.titanjr.rs.response.OrderOperateResponse;
import com.fangcang.titanjr.rs.response.OrderTransferFlowResponse;
import com.fangcang.titanjr.rs.response.RSPayOrderResponse;
import com.fangcang.titanjr.service.TitanCashierDeskService;
import com.fangcang.titanjr.service.TitanFinancialAccountService;
import com.fangcang.titanjr.service.TitanFinancialOrganService;
import com.fangcang.titanjr.service.TitanFinancialTradeService;
import com.fangcang.titanjr.service.TitanOrderService;
import com.fangcang.util.MyBeanUtil;
import com.fangcang.util.StringUtil;
import com.fangcang.titanjr.entity.TitanAccount;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("titanFinancialTradeService")
public class TitanFinancialTradeServiceImpl implements TitanFinancialTradeService {

    private static final Log log = LogFactory.getLog(TitanFinancialTradeServiceImpl.class);

    @Resource
    RSAccTradeManager rsAccTradeManager;

    @Resource
    RSPayOrderManager rsPayOrderManager;

    @Resource
    TitanFinancialTradeService titanFinancialTradeService;

    @Resource
    private HessianProxyBeanFactory hessianProxyBeanFactory;

    @Resource
    private TitanTransOrderDao titanTransOrderDao;

    @Resource
    private TitanOrderPayreqDao titanOrderPayreqDao;

    @Resource
    private TitanOrderService titanOrderService;

    @Resource
    private TitanTransferReqDao titanTransferReqDao;

    @Resource
    private TitanAccountDao titanAccountDao;

    @Resource
    private TitanDynamicKeyDao titanDynamicKeyDao;
    
    @Resource
    private TitanCashierDeskService titanCashierDeskService;
    
    @Resource 
    private TitanFinancialOrganService titanFinancialOrganService;
    
    @Resource 
    private TitanPayMethodConfigDao titanPayMethodDao;
    
    private FinanceSearchRemote financeSearchRemote;
    
    private FinanceOrderRemote financeOrderRemote;
    
    private HotelOrderSearchFacade hotelOrderSearchFacade;
    
    private static List<String> orderNoList = new ArrayList<String>();

    
    //落单
    @Override
    public TransOrderCreateResponse operateRSTransOrder(OrderRequest orderRequest) {
        TransOrderCreateResponse orderResponse = new TransOrderCreateResponse();
        try {
            OrderOperateResponse orderOperateResponse = getOrderId(orderRequest);
            //测试一下
            if (orderOperateResponse != null) {
                orderResponse.setResult(false);
                orderResponse.setReturnCode(orderOperateResponse.getReturnCode());
                orderResponse.setReturnMessage(orderOperateResponse.getReturnMsg());
                if ((CommonConstant.OPERATE_SUCCESS).equals(orderOperateResponse.getOperateStatus())) {
                	if(StringUtil.isValidString( orderOperateResponse.getOrderid())){
                    	if(orderRequest.getOpertype().equals(OperTypeEnum.Add_Order.getKey())){//添加订单
                    		orderRequest.setOrderid(orderOperateResponse.getOrderid());
                            TitanTransOrder titanTransOrder = orderRequest2TitanTransOrder(orderRequest);
                            orderResponse.setOrderNo(orderOperateResponse.getOrderid());
                            int row = 0;
	                        try{
	                        	row = titanTransOrderDao.insert(titanTransOrder);
	                    	}catch(Exception e){
	                         	log.error("融数成功,本地操作订单失败"+e.getMessage(),e);
	                        }
	                        if(row<1){
	                        	//TODO 写异常日志
	                        	OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(titanTransOrder.getOrderid(), "融数落单成功 本地记录失败", OrderExceptionEnum.TransOrder_Insert, JSON.toJSONString(titanTransOrder));
            	        		titanOrderService.saveOrderException(orderExceptionDTO);
	                        }
                    	}
                       
                	}else if(orderOperateResponse.getOrderOperateInfoList()!=null){
                   	    List<OrderOperateInfoDTO> orderOperateInfoList = new ArrayList<OrderOperateInfoDTO>();
                   		for(OrderOperateInfo OrderOperateInfo :orderOperateResponse.getOrderOperateInfoList()){
                   			OrderOperateInfoDTO orderOperateInfoDTO = new OrderOperateInfoDTO();
                   			MyBeanUtil.copyProperties(orderOperateInfoDTO, OrderOperateInfo);
                   			orderOperateInfoList.add(orderOperateInfoDTO);
                   			orderResponse.setOrderOperateInfoList(orderOperateInfoList);
                   		}
                	}
                	
                    orderResponse.putSuccess();
                }
                return orderResponse;
            }
            orderResponse.putSysError();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return orderResponse;
    }

    //生成并 保存单
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public TransOrderCreateResponse createTitanTransOrder(PaymentRequest paymentRequest) throws Exception {
        TransOrderCreateResponse orderResponse = new TransOrderCreateResponse();
        try {
            String orderid = null;
            //财务系统单号,充值就是充值的单号
            if (StringUtil.isValidString(paymentRequest.getPayOrderNo())) {
            	TransOrderResponse transOrderResponse = queryTransOrderByCode(paymentRequest.getPayOrderNo());
                //落单的订单不为null 则用tranid获取充值单
                if (transOrderResponse != null && null != transOrderResponse.getTransOrder()) {
                	//查看下单情况
                	TransOrderDTO transOrderDTO = transOrderResponse.getTransOrder();
                	//当该订单只有在处理中的时候
                	if(transOrderDTO.getStatusid().equals(OrderStatusEnum.Status_1.getStatus())||
                			transOrderDTO.getStatusid().equals(OrderStatusEnum.Status_4.getStatus())){//支付单在处理中则判断充值单
                		//当充值金额不一样的时候
                    	boolean flag = validateIsUpdateOrder(paymentRequest,transOrderDTO);
                    	if(flag){//充值金额不一样则将原单设为失效，重新生成订单
                    		 TitanOrderPayreq titanOrderPayreq = new TitanOrderPayreq();
                             titanOrderPayreq.setTransorderid(transOrderDTO.getTransid());
                             titanOrderPayreq = queryOrderPayReqByTransOrderId(titanOrderPayreq);
                             //充值订单不为空，则判断充值单的状态，如果是失败则生成系统单号
                             if (titanOrderPayreq != null) {
                                 if (ReqstatusEnum.Status_2.getStatus() == titanOrderPayreq.getReqstatus()) { //充值状态成功则直接返回
                                     orderResponse.putErrorResult("充值成功，请勿重复充值");
                                     return orderResponse;
                                 } else if (ReqstatusEnum.Status_1.getStatus() == titanOrderPayreq.getReqstatus()) {//处理中判断落单时间和过期时间
                                     //获取当前时间与订单时间的秒数差
                                     long times = DateUtil.diffSecondByTime(titanOrderPayreq.getOrderTime(), DateUtil.sdf11.format(new Date()));
                                     if ( times < this.getExpireTime(titanOrderPayreq)) {//未过期 获取当前单号
                                         orderid = titanOrderPayreq.getOrderNo();
                                     } else {
                                     	TitanTransOrder titanTransOrder = new TitanTransOrder();
                                     	titanTransOrder.setStatusid(OrderStatusEnum.Status_5.getStatus());
                                     	titanTransOrder.setTransid(transOrderResponse.getTransOrder().getTransid());
                                     	int row =0;
                                     	try{
                                     		row = titanTransOrderDao.update(titanTransOrder);
                                     	}catch(Exception e){
                                     		log.error("该订单失效设置失败"+e.getMessage(),e);
                                     	}
                                     	if(row<1){
                                     		//TODO 写异常单
                                     		OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(transOrderDTO.getOrderid(), "下单 设置订单失效", OrderExceptionEnum.TransOrder_update, JSON.toJSONString(OrderStatusEnum.Status_3));
                        	        		titanOrderService.saveOrderException(orderExceptionDTO);
                                     	}
                                     }
                                 }
                             } else {//订单有效，无充值请求则返回该单号进行充值操作
                                 orderid = transOrderResponse.getTransOrder().getOrderid();
                             }
                    	}
                       
                	}else if(OrderStatusEnum.Status_2.getStatus().equals(transOrderDTO.getStatusid())||
                			OrderStatusEnum.Status_3.getStatus().equals(transOrderDTO.getStatusid())){
                		   orderResponse.putErrorResult("支付成功，请勿重复支付");
                		   //回调财务
                		   confirmFinance(transOrderDTO);
                           return orderResponse;
                	}
                }
                
                if ( !StringUtil.isValidString(orderid) ) { //如果订单号为空，则直接生成订单号
                    paymentRequest.setUserorderid(OrderGenerateService.genSyncUserOrderId());
                    OrderRequest orderRequest = convertorToTitanOrderRequest(paymentRequest);
                    orderResponse = operateRSTransOrder(orderRequest);
                } else {
                    orderResponse.setOrderNo(orderid);
                    orderResponse.putSuccess();
                }
            }
        } catch (Exception e) {
            log.error("创建泰坦金服交易单失败", e);
            throw new Exception(e);
        }
        return orderResponse;
    }

    /**
     * 根据TransOrderid获取财务单
     *
     * @param titanOrderPayreq
     * @return
     * @throws Exception 
     */
    private TitanOrderPayreq queryOrderPayReqByTransOrderId(TitanOrderPayreq titanOrderPayreq) throws Exception {
    	try{
    		 TitanOrderPayreq OrderPayreq = null;
	         TitanOrderPayreqParam condition = new TitanOrderPayreqParam();
	         condition.setTransorderid(titanOrderPayreq.getTransorderid());
	         condition.setOrderNo(titanOrderPayreq.getOrderNo());
	         PaginationSupport<TitanOrderPayreq> paginationSupport = new PaginationSupport<TitanOrderPayreq>();
	         paginationSupport.setOrderBy("orderTime desc");
	         titanOrderPayreqDao.selectForPage(condition, paginationSupport);
	         List<TitanOrderPayreq> titanOrderPayreqList = paginationSupport.getItemList();
	         if (titanOrderPayreqList != null && titanOrderPayreqList.size() > 0) {
	             OrderPayreq = titanOrderPayreqList.get(0);
	         }
	        return OrderPayreq;
    	}catch(Exception e){
    		throw new Exception(e);
    	}
    }

    /**
     * 查询落单记录
     *
     * @param payOrderNo
     * @return
     * @throws Exception 
     */
    private TransOrderResponse queryTransOrderByCode(String payOrderNo) throws Exception {
    	try{
    		TransOrderResponse transOrderResponse =null;
            TransOrderRequest transOrderRequest = new TransOrderRequest();
            transOrderRequest.setPayorderno(payOrderNo);
            List<TransOrderDTO> transOrderDTOList = titanOrderService.queryTransOrder(transOrderRequest);
            
            if (transOrderDTOList != null && transOrderDTOList.size() > 0) {
            	TransOrderDTO transOrderDTO = transOrderDTOList.get(0);
            	transOrderResponse = new TransOrderResponse();
            	transOrderResponse.setTransOrder(transOrderDTO);
            }
            return transOrderResponse;
    	}catch(Exception e){
    		throw new Exception(e);
    	}
    
    }

    /**
     * 落单操作
     *
     * @param orderRequest
     * @return
     * @throws Exception
     * @author fangdaikang
     */

    private OrderOperateResponse getOrderId(OrderRequest orderRequest) throws Exception {
        try {
            OrderOperateRequest req = new OrderOperateRequest();
            req.setUserid(orderRequest.getUserid());                                // 接入机构中设置的用户ID
            req.setConstid(CommonConstant.RS_FANGCANG_CONST_ID);                            // 机构码
            req.setOrdertypeid(orderRequest.getOrdertypeid());                            // 基础业务为B，扩展业务待定 M70001棉庄订金支付
            req.setProductid(orderRequest.getProductId());                        // 产品号
            req.setOpertype(orderRequest.getOpertype());                                // 操作类型（修改：2,新增：1,取消4,查询3）
            req.setOrderdate(new Date());                        // 订单日期
            req.setOrdertime(new Date());                        // 订单时间
            req.setUserorderid(orderRequest.getUserorderid());                            // 用户订单编号
            req.setAmount(orderRequest.getAmount());
            req.setGoodsname(orderRequest.getGoodsname());                            // 商品名称
            req.setGoodsdetail(orderRequest.getGoodsdetail());                            // 商品描述
            req.setNumber(orderRequest.getNumber());                                    // 商品数量
            req.setUnitprice(orderRequest.getUnitprice());                                // 手续费
            req.setAdjusttype(orderRequest.getAdjusttype());                            // 调整类型
            req.setAdjustcontent(orderRequest.getAdjustcontent());                        // 调整内容
            req.setUserrelateid(orderRequest.getUserrelateid());                            // 关联用户id（若有第三方则必须填写）
            return rsAccTradeManager.operateOrder(req);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    //转账
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public TransferResponse transferAccounts(TransferRequest transferRequest) throws Exception {
    	log.info("进入转账，入参"+JSON.toJSONString(transferRequest));
    	TransferResponse accTradeResponse = new TransferResponse();
    	String payOrderNo = null;
        try {
            AccountTransferRequest accountRequest = transferRequest2AccountTransferRequest(transferRequest);
            if (accountRequest != null) {
                TitanTransferReq titanTransferReq = transferRequest2TitanTransferReq(transferRequest);
                accTradeResponse.setRequestNo(titanTransferReq.getRequestno());
                if (titanTransferReq != null) {
                	Integer transid =null;
            		TransOrderDTO transOrderDTO = getTransidByOrderId(transferRequest.getOrderid());
                    if(transOrderDTO !=null){
                    	transid = transOrderDTO.getTransid();
                    	payOrderNo = transOrderDTO.getPayorderno();
                    }
                    //获取落单时的订单id
                    if (transid != null && StringUtil.isValidString(payOrderNo)) {
                        titanTransferReq.setTransorderid(transid);
                        titanTransferReq.setPayorderno(payOrderNo);
                        lockOutTradeNoList(payOrderNo);
//                        synchronized (payOrderNo) {//锁定支付单
                        	//查询该单号是否已经有转账单
                        	TitanTransferReq titanTransfer = queryTransfer(payOrderNo);
                        	boolean flag =false;
                        	if(titanTransfer ==null){//判断转账是否已经进行
                        		flag = titanTransferReqDao.insert(titanTransferReq) > 0 ? true : false;
                        	}else{
                        		if(TransferReqEnum.Status_2.getStatus()!=titanTransfer.getStatus().intValue()){
                        			titanTransferReq.setTransferreqid(titanTransfer.getTransferreqid());
                        			flag = titanTransferReqDao.update(titanTransferReq) > 0 ? true : false;
                        		}
                        	}
                            if (flag) {
                                AccountTransferResponse accountTransferResponse = rsAccTradeManager.accountBalanceTransfer(accountRequest);
                                log.info("转账结果"+JSON.toJSONString(accountTransferResponse));
                                if (accountTransferResponse != null) {
                                    if (CommonConstant.OPERATE_SUCCESS.equals(accountTransferResponse.getOperateStatus())) {
                                        titanTransferReq.setStatus(TransferReqEnum.Status_2.getStatus());
                                        accTradeResponse.putSuccess();
                                    } else {
                                    	titanTransferReq.setStatus(TransferReqEnum.Status_3.getStatus());
                                        accTradeResponse.putErrorResult(accountTransferResponse.getRetcode(), accountTransferResponse.getRetmsg());
                                    	//转账是否成功，重复佐证  待确认
                                    	ROPErrorEnum ropErrorEnum = ROPErrorEnum.getROPErrorEnumByCode(accountTransferResponse.getReturnCode());
                                        if(ropErrorEnum !=null){//若错误提示是ROP连接等错误需要重复确认
                                        	AccountTransferFlowRequest accountTransferFlowRequest = new AccountTransferFlowRequest();
                                        	accountTransferFlowRequest.setRequestNo(accountRequest.getRequestno());
                                        	accountTransferFlowRequest.setProductId(accountRequest.getProductid());
                                        	accountTransferFlowRequest.setUserId(accountRequest.getUserid());
                                        	if(this.confirmTransAccountSuccess(accountTransferFlowRequest)){//确认转账成功
                                        		  titanTransferReq.setStatus(TransferReqEnum.Status_2.getStatus());
                                                  accTradeResponse.putSuccess();
                                        	 }
                                        }
                                    }
                                    try{
                                    	 titanTransferReqDao.update(titanTransferReq);
                                    }catch(Exception e){
                                    	log.error("更新转账记录失败"+e.getMessage(),e);
                                    	//TODO 写异常单日志
                                    	OrderExceptionDTO orderExceptionDTO = new OrderExceptionDTO(transOrderDTO.getOrderid(), "转账成功 更新转账记录失败", OrderExceptionEnum.Transfer_Update, JSON.toJSONString(titanTransferReq));
                    	        		titanOrderService.saveOrderException(orderExceptionDTO);
                                    }
                                }
                            }else{
                            	log.error("充值下单失败，对应的落单id："+titanTransferReq.getTransorderid());
                            }
                            unlockOutTradeNoList(payOrderNo);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e);
        }finally{
        	 unlockOutTradeNoList(payOrderNo);
        }
        return accTradeResponse;
    }
    
    private TitanTransferReq queryTransfer(String payOrderNo){
    	TitanTransferReqParam titanTransferReqParam = new TitanTransferReqParam();
    	titanTransferReqParam.setPayorderno(payOrderNo);
    	List<TitanTransferReq>  titanTransferList= titanTransferReqDao.queryTitanTransferReq(titanTransferReqParam);
    	if(titanTransferList!=null && titanTransferList.size()>0){
    		TitanTransferReq titanTransferReq = titanTransferList.get(0);
    		if(titanTransferReq!=null){
    			return titanTransferList.get(0);
    		}
    	}
    	return null;
    }
    
    //回调财务
    @Override
    public boolean confirmFinance(TransOrderDTO transOrderDTO){
    	FinanceOrderConfirmRequest financeOrderConfirmRequest = new FinanceOrderConfirmRequest();
    	try{
    		//查询payOrderNo和merchancode
        	if(transOrderDTO !=null){
        		financeOrderConfirmRequest.setFinanceCode(transOrderDTO.getPayorderno());
        		financeOrderConfirmRequest.setMerchantCode(transOrderDTO.getMerchantcode());
        		financeOrderConfirmRequest.setStatus(TradeStatusEnum.SUCCESS);
        		financeOrderConfirmRequest.setSerialNumber(OrderGenerateService.genResquestNo());
        		financeOrderConfirmRequest.setConfirmManCode(transOrderDTO.getUserid());
        		financeOrderConfirmRequest.setConfirmMan("system");
        		financeOrderConfirmRequest.setConfirmManCode("1");
        	}
        	//财务付款单号
            String msg = getFinanceOrderRemote().confirmFinanceOrder(financeOrderConfirmRequest);
            if(transOrderDTO.getPayorderno().equals(msg)){
            	return true;
            }
    	}catch(Exception e){
    		log.error("回调财务异常"+e.getMessage(),e);
    	}
    	return false;
    }
    
    //查询转账
    @Override
    public boolean confirmTransAccountSuccess(AccountTransferFlowRequest accountTransferFlowRequest) {
    	try{
    		if(accountTransferFlowRequest !=null){
    			OrderTransferFlowRequest orderTransferFlowRequest = new OrderTransferFlowRequest();
        		orderTransferFlowRequest.setUserid(accountTransferFlowRequest.getUserId());
        		orderTransferFlowRequest.setConstid(CommonConstant.RS_FANGCANG_CONST_ID);
        		orderTransferFlowRequest.setRequestno(accountTransferFlowRequest.getRequestNo());
        		orderTransferFlowRequest.setProductid(accountTransferFlowRequest.getProductId());
        		OrderTransferFlowResponse orderTransferFlowResponse = rsAccTradeManager.queryOrderTranferFlow(orderTransferFlowRequest);
            	if(CommonConstant.OPERATE_SUCCESS.equals(orderTransferFlowResponse.getOperateStatus())){
            		return true;
            	}
    		}
    		
    	}catch(Exception e){
    		log.error("确认转账是否成功异常"+e.getMessage(),e);
    	}
    	return false;
    }
    
    //获取Transid    
    private TransOrderDTO getTransidByOrderId(String orderId) throws Exception{
    	try{
    		if(StringUtil.isValidString(orderId)){
    			TransOrderRequest transOrderRequest = new TransOrderRequest();
                transOrderRequest.setOrderid(orderId);
                TransOrderDTO transOrderDTO = titanOrderService.queryTransOrderDTO(transOrderRequest);
                if (transOrderDTO != null) {//获取transid
                    return transOrderDTO;
                }
    		}
    	}catch(Exception e){
    		log.error("获取TransId错误"+e.getMessage(),e);
    		throw new Exception(e);
    	}
    	return null;
    }
    
    private AccountTransferRequest transferRequest2AccountTransferRequest(TransferRequest transferRequest) throws Exception {
        AccountTransferRequest account = null;
        try {
            if (transferRequest != null) {
                account = new AccountTransferRequest();
                account.setAmount(transferRequest.getAmount());
                if (transferRequest.getTransfertype() != null) {
                    account.setTransfertype(transferRequest.getTransfertype().getKey());    //1:子账户转账
                }
                if (transferRequest.getConditioncode() != null) {
                    account.setConditioncode(transferRequest.getConditioncode().getKey());        //1:落单
                }
                account.setMerchantcode(CommonConstant.RS_FANGCANG_CONST_ID);                            //转入方机构号
                account.setProductid(transferRequest.getProductId());                            //转入方产品号
                account.setUserid(transferRequest.getUserid());                                        //转出的用户
                //需要生成的业务单号
                account.setRequestno(transferRequest.getRequestno());                                    //业务订单号
                account.setRequesttime(transferRequest.getRequesttime());                //请求时间
                account.setAmount(transferRequest.getAmount());                                        //金额
                account.setUserfee(transferRequest.getUserfee());                                    //手续费
                account.setIntermerchantcode(CommonConstant.RS_FANGCANG_CONST_ID);                    //接收机构号
                account.setInterproductid(transferRequest.getInterproductid());                        //接收方产品号
                account.setUserrelateid(transferRequest.getUserrelateid());       //接收方用户Id
                account.setProductid(transferRequest.getProductId());
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
        return account;
    }

    //将transferRequest转换为
    private TitanTransferReq transferRequest2TitanTransferReq(TransferRequest transferRequest) throws Exception {
        TitanTransferReq titanTransferReq = null;
        try {
            if (transferRequest != null) {
                titanTransferReq = new TitanTransferReq();
                if (StringUtil.isValidString(transferRequest.getAmount())) {
                    titanTransferReq.setAmount(Double.parseDouble(transferRequest.getAmount()));
                }
                if (transferRequest.getTransfertype() != null) {
                    titanTransferReq.setTransfertype(Integer.parseInt(transferRequest.getTransfertype().getKey()));    //1:子账户转账
                }
                titanTransferReq.setCreatetime(new Date());
                titanTransferReq.setCreator(transferRequest.getCreator());
                titanTransferReq.setMerchantcode(CommonConstant.RS_FANGCANG_CONST_ID);
                titanTransferReq.setProductid(transferRequest.getProductId());
                titanTransferReq.setIntermerchantcode(CommonConstant.RS_FANGCANG_CONST_ID);
                titanTransferReq.setInterproductid(transferRequest.getInterproductid());
                titanTransferReq.setRequestno(transferRequest.getRequestno());
                if (transferRequest.getRequesttime() != null) {
                    titanTransferReq.setRequesttime(DateUtil.StringToDate(transferRequest.getRequesttime(), "yyyy-MM-dd HH:mm:ss"));
                }
                if (transferRequest.getStatus() != null) {
                    titanTransferReq.setStatus(Integer.parseInt(transferRequest.getStatus()));
                }
                if (transferRequest.getTransfertype() != null) {
                    titanTransferReq.setTransfertype(Integer.parseInt(transferRequest.getTransfertype().getKey()));
                }
                if (transferRequest.getUserfee() != null) {
                    titanTransferReq.setUserfee(Double.parseDouble(transferRequest.getUserfee()));
                }
                titanTransferReq.setUserid(transferRequest.getUserid());
                titanTransferReq.setUserrelateid(transferRequest.getUserrelateid());
                titanTransferReq.setStatus(TransferReqEnum.Status_2.getStatus());
                if (transferRequest.getConditioncode() != null) {
                    titanTransferReq.setConditioncode(Integer.parseInt(transferRequest.getConditioncode().getKey()));
                }

            }
        } catch (Exception e) {
            throw new Exception(e);
        }
        return titanTransferReq;
    }


    //   充值
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public RechargeResponse generateRechargePage(RechargePageRequest rechargePageRequest) throws Exception {
        RechargeResponse rechargeResponse = new RechargeResponse();
        try {
            //获取充值的请求参数
            RechargeRequest rechargeRequest = convertorToRechargeRequest(rechargePageRequest);
            if (rechargeRequest != null) {
                //将充值请求数据转换为融数充值请求数据
                RSPayOrderRequest rsPayOrderRequest = convertorToRSPayOrderRequest(rechargeRequest);
                if (rsPayOrderRequest != null) {
                    TitanOrderPayreq titanOrder = new TitanOrderPayreq();
                    titanOrder.setOrderNo(rechargeRequest.getOrderNo());
                    titanOrder = queryOrderPayReqByTransOrderId(titanOrder);
                    //该单号已经在融数下了充值单则拿以前的充值单，否则新建充值单
                    if (titanOrder != null) {
                        rsPayOrderRequest.setOrderTime(titanOrder.getOrderTime());
                    } else {
                        TitanOrderPayreq titanOrderPayreq = convertorToTitanOrderPayReq(rechargeRequest);
                        if (titanOrderPayreq != null) {
                            titanOrderPayreq.setReqstatus(ReqstatusEnum.Status_1.getStatus());
                            titanOrderPayreqDao.insert(titanOrderPayreq);
                        }
                    }
                    log.info("充值获取参数的入参:"+JSON.toJSONString(rsPayOrderRequest));
                    RSPayOrderResponse response = rsPayOrderManager.getPayPage(rsPayOrderRequest);
                    log.info("充值获取参数的结果:"+JSON.toJSONString(response));
                    if (response != null) {
                    	rechargeResponse.putErrorResult(response.getReturnCode(), response.getReturnMsg());
                        if (CommonConstant.OPERATE_SUCCESS.equals(response.getOperateStatus())) {
                            rechargeResponse.putSuccess();
                            RechargeDataDTO rechargeDataDTO = new RechargeDataDTO();
                            MyBeanUtil.copyProperties(rechargeDataDTO, response.getRsPayOrderRequest());
                            rechargeResponse.setRechargeDataDTO(rechargeDataDTO);
                        }
                        return rechargeResponse;
                    }
                }
            }
        } catch (Exception e) {
            log.error("生成金服直连支付页面异常"+e.getMessage(), e);
            throw new Exception(e);
        }
        return rechargeResponse;
    }

    /**
     * 将充值的请求转换为融数的请求参数
     *
     * @param rechargeRequest
     * @return
     * @author fangdaikang
     */
    private RSPayOrderRequest convertorToRSPayOrderRequest(RechargeRequest rechargeRequest) {
        RSPayOrderRequest req = null;
        if (rechargeRequest != null) {
            req = new RSPayOrderRequest();
            req.setMerchantNo(CommonConstant.RS_FANGCANG_CONST_ID);
            req.setOrderNo(rechargeRequest.getOrderNo());
            req.setProductNo(rechargeRequest.getProductNo());
            req.setProductName(rechargeRequest.getProductName());
            req.setProductDesc(rechargeRequest.getProductDesc());
            req.setProductNum(rechargeRequest.getProductNum());

            req.setOrderAmount(rechargeRequest.getOrderAmount());
            req.setPayType(rechargeRequest.getPayType());
            req.setAmtType(rechargeRequest.getAmtType());
            req.setBankInfo(rechargeRequest.getBankInfo());
            req.setPayerAcount(rechargeRequest.getPayerAcount());
            req.setPayerName(rechargeRequest.getPayerName());
            req.setPayerPhone(rechargeRequest.getPayerPhone());
            req.setPayerMail(rechargeRequest.getPayerMail());
            req.setPageUrl(rechargeRequest.getPageUrl());
            req.setNotifyUrl(rechargeRequest.getNotifyUrl());
            req.setOrderTime(rechargeRequest.getOrderTime());
            if (rechargeRequest.getOrderExpireTime() != null) {
                req.setOrderExpireTime(rechargeRequest.getOrderExpireTime().toString());
            }
            req.setOrderMark(rechargeRequest.getOrderMark());
            req.setExpand(rechargeRequest.getExpand());
            req.setExpand2(rechargeRequest.getExpand2());
            req.setSignType(rechargeRequest.getSignType());
            req.setBusiCode(rechargeRequest.getBusiCode());
            req.setVersion(rechargeRequest.getVersion());
            req.setCharset(rechargeRequest.getCharset());
        }
        return req;
    }


    /**
     * 将财务工单和调用者传入的参数转换为  落单请求参数
     *
     * @param financialOrderResponse
     * @return
     * @throws Exception
     */
    private OrderRequest convertorToTitanOrderRequest(PaymentRequest paymentRequest) throws Exception {
        OrderRequest orderRequest = null;
        try {
        	  orderRequest = new OrderRequest();
            if(paymentRequest !=null){
            	 orderRequest.setProductId(paymentRequest.getProductId());
            	 orderRequest.setUserid(paymentRequest.getUserid());
                 orderRequest.setAmount(NumberUtil.covertToCents(paymentRequest.getPayAmount()));
                 orderRequest.setNumber(paymentRequest.getNumber());
                 orderRequest.setAdjustcontent(paymentRequest.getAdjustcontent());
                 orderRequest.setAdjusttype(paymentRequest.getAdjusttype());
                 orderRequest.setUserorderid(OrderGenerateService.genSyncUserOrderId());
                 orderRequest.setUserrelateid(paymentRequest.getUserrelateid());
                 orderRequest.setInterProductid(paymentRequest.getInterProductid());
                 orderRequest.setCreator(paymentRequest.getCreator());
                 orderRequest.setMerchantCode(paymentRequest.getMerchantcode());
                 orderRequest.setPayOrderNo(paymentRequest.getPayOrderNo());
                 orderRequest.setTradeamount(paymentRequest.getTradeamount());
                 if(CashierDeskTypeEnum.RECHARGE.deskCode.equals(paymentRequest.getPaySource())){
                	 orderRequest.setPayeemerchant(paymentRequest.getUserid());
                 }else{
                	 orderRequest.setPayeemerchant(paymentRequest.getUserrelateid());
                     orderRequest.setPayermerchant(paymentRequest.getUserid());
                 }
                 if(StringUtil.isValidString(paymentRequest.getIsEscrowed())){
                	 orderRequest.setIsEscrowedPayment(paymentRequest.getIsEscrowed()); 
                 }
                 if(StringUtil.isValidString(paymentRequest.getEscrowedDate())){
               	  orderRequest.setEscrowedDate(DateUtil.sdf.parse(paymentRequest.getEscrowedDate()));
                }
                 if (paymentRequest.getOperType() != null) {
                     orderRequest.setOpertype(paymentRequest.getOperType().getKey());
                 }
                 if (paymentRequest.getOrdertype() != null) {
                     orderRequest.setOrdertypeid(paymentRequest.getOrdertype().getKey());
                 }
                 if(paymentRequest.getUnitprice() !=null){
                 	 orderRequest.setUnitprice(NumberUtil.covertToCents(paymentRequest.getUnitprice()));
                 }
                 
                 if(CashierDeskTypeEnum.B2B_DESK.deskCode.equals(paymentRequest.getPaySource())){
           		  GDPOrderResponse gDPOrderResponse =  getGDPOrderDTO(paymentRequest.getPayOrderNo());
           		  if(gDPOrderResponse.getgDPOrderDTO() !=null){//有待扩展
           			  orderRequest.setGoodsdetail(gDPOrderResponse.getgDPOrderDTO().getGoodName());
           			  orderRequest.setBusinessordercode(gDPOrderResponse.getgDPOrderDTO().getOrderCode());
           			  orderRequest.setGoodsname("GDP付款单");
           		  }
           	  }else if(CashierDeskTypeEnum.SUPPLY_DESK.deskCode.equals(paymentRequest.getPaySource())){
           		  FinancialOrderRequest financialOrderRequest = new FinancialOrderRequest();
           		  financialOrderRequest.setMerchantcode(paymentRequest.getMerchantcode());
           		  financialOrderRequest.setOrderNo(paymentRequest.getPayOrderNo());
           		  FinancialOrderResponse  financialOrderResponse = queryFinanceOrderDetail(financialOrderRequest);
           		  if (financialOrderResponse != null && financialOrderResponse.isResult()) {
                     orderRequest.setGoodsdetail(financialOrderResponse.getContent());
                     orderRequest.setOrderdate(financialOrderResponse.getCreateDate());
                     orderRequest.setOrdertime(financialOrderResponse.getCreateDate());
                     orderRequest.setBusinessordercode(financialOrderResponse.getOrderCode());
                     orderRequest.setGoodsname("财务支付单");
           		  }
           	  }else{
           		orderRequest.setGoodsname("充值单");
           	  }
            }
            
        } catch (Exception e) {
            throw new Exception(e);
        }
        return orderRequest;
    }
    

    /**
     * 将财务单个和调用者传入的参数封装为  支付请求
     *
     * @param rechargePageRequest
     * @return
     * @throws Exception
     */
    private RechargeRequest convertorToRechargeRequest(RechargePageRequest rechargePageRequest) throws Exception {
        RechargeRequest rechargeRequest = null;
        try {
            //根据系统生成的单号在本地查询订单信息
            TransOrderRequest transOrderRequest = new TransOrderRequest();
            transOrderRequest.setOrderid(rechargePageRequest.getOrderid());

            //获取本地落单信息
                //为什么取第一个单，因为若单是成功或者失败，在下单的时候就获取不到单号，只有在处理中的单才能
        	TransOrderDTO transOrderDTO = titanOrderService.queryTransOrderDTO(transOrderRequest);
            if (transOrderDTO != null) {
                rechargeRequest = new RechargeRequest();
                rechargeRequest.setTransorderid(transOrderDTO.getTransid());
                rechargeRequest.setUserid(rechargePageRequest.getUserid());
                rechargeRequest.setOrderNo(rechargePageRequest.getOrderid());
                if (transOrderDTO.getAmount() != null) {
                   rechargeRequest.setOrderAmount(transOrderDTO.getAmount().toString());
                }
                //产品名称-产品内容
                rechargeRequest.setProductName(transOrderDTO.getGoodsname());
                //待定
                rechargeRequest.setProductNo("");
                //产品描述对应财务订单的备注
                rechargeRequest.setProductDesc(transOrderDTO.getGoodsdetail());

                if (rechargePageRequest.getNumber() != null) {
                    rechargeRequest.setProductNum(rechargePageRequest.getNumber().toString());
                }
                rechargeRequest.setAmtType(AmtTypeEnum.RMB.getKey());
                rechargeRequest.setPayerAcount(rechargePageRequest.getPayerAcount());
                rechargeRequest.setPayerName(rechargePageRequest.getPayerName());
                rechargeRequest.setPayerPhone(rechargePageRequest.getPayerPhone());
                rechargeRequest.setPayerMail(rechargePageRequest.getPayerMail());
                rechargeRequest.setBankInfo(rechargePageRequest.getBankInfo());
                rechargeRequest.setPageUrl(rechargePageRequest.getPageUrl());
                rechargeRequest.setNotifyUrl(rechargePageRequest.getNotifyUrl());
                //支付时的orderTime是指落单的时间还是支付的时间
                rechargeRequest.setOrderTime(DateUtil.sdf5.format(new Date()));
                if (rechargePageRequest.getOrderExpireTime() != null) {
                    rechargeRequest.setOrderExpireTime(rechargePageRequest.getOrderExpireTime());
                }
                if (rechargePageRequest.getOrderMark() != null) {
                    rechargeRequest.setOrderMark(rechargePageRequest.getOrderMark().getKey());
                }
                rechargeRequest.setExpand(rechargePageRequest.getExpand());
                rechargeRequest.setExpand2(rechargePageRequest.getExpand2());
                if (rechargePageRequest.getSignType() != null) {
                    rechargeRequest.setSignType(rechargePageRequest.getSignType().getKey());
                }
                if (rechargePageRequest.getBusiCode() != null) {
                    rechargeRequest.setBusiCode(rechargePageRequest.getBusiCode().getKey());
                }
                if (rechargePageRequest.getOrderMark() != null) {
                    rechargeRequest.setOrderMark(rechargePageRequest.getOrderMark().getKey());
                }
                if (rechargePageRequest.getPayType() != null) {
                    rechargeRequest.setPayType(rechargePageRequest.getPayType().getKey());
                }
                if (rechargePageRequest.getCharset() != null) {
                    rechargeRequest.setCharset(rechargePageRequest.getCharset().getKey());
                }
            }

        } catch (Exception e) {
        	 e.printStackTrace();
            throw new Exception(e);
           
        }
        return rechargeRequest;
    }

    //获取财务单
    @Override
    public FinancialOrderResponse queryFinanceOrderDetail(FinancialOrderRequest financialOrderRequest) {
    	FinancialOrderResponse financialOrderResp = new FinancialOrderResponse();
        try {
            FinanceOrderQuery financeOrderQuery = new FinanceOrderQuery();
            if (financialOrderRequest != null) {
                financeOrderQuery.setMerchantCode(financialOrderRequest.getMerchantcode());//商家编号
                financeOrderQuery.setFinanceCode(financialOrderRequest.getOrderNo());//支付工单号
                log.info("获取财务工单入参:"+JSON.toJSONString(financialOrderRequest));
                FinanceOrderResponse resp = getFinanceSearchRemote().searchOrderFinanceOrder(financeOrderQuery);
                log.info("获取财务工单结果:"+JSON.toJSONString(resp));
                if (resp != null) {
                    if (CommonConstant.RETURN_SUCCESS.equals(resp.getResult())) {
                        financialOrderResp = convertToTitanFinancialResp(resp);
                        financialOrderResp.putSuccess();
                    } else {
                        financialOrderResp.setResult(false);
                        financialOrderResp.setReturnCode(resp.getErrCode());
                        financialOrderResp.setReturnMessage(resp.getReason());
                    }
                }
            }
        } catch (Exception e) {
            log.error("查询财务工单异常", e);
            financialOrderResp.putSysError();
        }
        return financialOrderResp;
    }

    /**
     * 将财务工单转换为泰坦金服所需工单
     * 有些字段不需要
     *
     * @param financeOrderResponse
     * @throws Exception
     * @author fangdaikang
     */
    private FinancialOrderResponse convertToTitanFinancialResp(FinanceOrderResponse financeOrderResponse) throws Exception {
        FinancialOrderResponse result = new FinancialOrderResponse();
        if (financeOrderResponse != null) {
            result.setAccountData(financeOrderResponse.getAccountData());
            result.setBankAccount(financeOrderResponse.getBankAccount());
            result.setBankName(financeOrderResponse.getBankName());
            result.setContent(financeOrderResponse.getContent());
            result.setCreateDate(financeOrderResponse.getCreateDate());
            result.setCreator(financeOrderResponse.getCreator());
            if (financeOrderResponse.getCurrency() != null) {
                result.setCurrency(AmtTypeEnum.getAmtTypeEnum(financeOrderResponse.getCurrency()));
            }
            result.setFinanceCode(financeOrderResponse.getFinanceCode());
            result.setFinanceId(financeOrderResponse.getFinanceId());
            result.setFinanceLogList(financeOrderResponse.getFinanceLogList());
            result.setNote(financeOrderResponse.getNote());
            result.setOrderCode(financeOrderResponse.getOrderCode());
            result.setPassage(financeOrderResponse.getPassage());
            result.setPayAmount(financeOrderResponse.getPayAmount());
            result.setType(financeOrderResponse.getType());
            result.setInAccountCode(financeOrderResponse.getInAccountCode());
            result.setInAccountName(financeOrderResponse.getInAccountName());
            result.setOutAccountCode(financeOrderResponse.getOutAccountCode());
            result.setOutAccountName(financeOrderResponse.getOutAccountName());
        }
        return result;
    }


    /**
     * 落单时的请求工单封装为 本地TitanTransOrder
     *
     * @param orderRequest
     * @return
     * @throws Exception
     */
    private TitanTransOrder orderRequest2TitanTransOrder(OrderRequest orderRequest) throws Exception {
        TitanTransOrder titanTransOrder = null;
        try {
            titanTransOrder = new TitanTransOrder();
            if (orderRequest != null) {
                titanTransOrder.setAdjustcontent(orderRequest.getAdjustcontent());
                titanTransOrder.setAdjusttype(orderRequest.getAdjusttype());
                if (orderRequest.getAmount() != null) {
                    titanTransOrder.setAmount(Long.parseLong(orderRequest.getAmount()));
                }
                titanTransOrder.setBusinessordercode(orderRequest.getBusinessordercode());
                titanTransOrder.setConstid(CommonConstant.RS_FANGCANG_CONST_ID);
                titanTransOrder.setCreatetime(new Date());
                titanTransOrder.setCreator(orderRequest.getCreator());

                titanTransOrder.setGoodscnt(orderRequest.getNumber());
                titanTransOrder.setGoodsdetail(orderRequest.getGoodsdetail());
                titanTransOrder.setGoodsname(orderRequest.getGoodsname());
                titanTransOrder.setOrderdate(orderRequest.getOrderdate());

                titanTransOrder.setOrderid(orderRequest.getOrderid());
                titanTransOrder.setOrdertime(orderRequest.getOrdertime());
                titanTransOrder.setOrdertypeid(orderRequest.getOrdertypeid());
                titanTransOrder.setProductid(orderRequest.getProductId());
                titanTransOrder.setProvider(null);
                titanTransOrder.setStatusid(OrderStatusEnum.Status_1.getStatus());
                if (orderRequest.getUnitprice() != null) {
                    titanTransOrder.setUnitprice(Long.parseLong(orderRequest.getUnitprice()));
                }
                titanTransOrder.setUserid(orderRequest.getUserid());
                titanTransOrder.setUserorderid(orderRequest.getUserorderid());
                titanTransOrder.setInterproductid(orderRequest.getInterProductid());
                titanTransOrder.setUserrelateid(orderRequest.getUserrelateid());
                titanTransOrder.setReceivablefee(null);
                titanTransOrder.setReceivedfee(null);
                titanTransOrder.setIsEscrowedPayment(orderRequest.getIsEscrowedPayment());
                titanTransOrder.setEscrowedDate(orderRequest.getEscrowedDate());
                titanTransOrder.setPayorderno(orderRequest.getPayOrderNo());
                titanTransOrder.setMerchantcode(orderRequest.getMerchantCode());
                titanTransOrder.setPayeemerchant(orderRequest.getPayeemerchant());
                titanTransOrder.setPayermerchant(orderRequest.getPayermerchant());
                titanTransOrder.setTradeamount(orderRequest.getTradeamount());
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

        return titanTransOrder;
    }

    /**
     * 支付请求 转换为TitanOrderPayreq
     *
     * @param rechargeRequest
     * @return
     * @throws Exception
     */
    private TitanOrderPayreq convertorToTitanOrderPayReq(RechargeRequest rechargeRequest) throws Exception {
        TitanOrderPayreq titanOrderPayreq = null;
        try {
            titanOrderPayreq = new TitanOrderPayreq();
            titanOrderPayreq.setTransorderid(rechargeRequest.getTransorderid());
            titanOrderPayreq.setAmtType(rechargeRequest.getAmtType());
            titanOrderPayreq.setBankInfo(rechargeRequest.getBankInfo());
            titanOrderPayreq.setBusiCode(rechargeRequest.getBusiCode());
            if (rechargeRequest.getCharset() != null) {
                titanOrderPayreq.setCharset(Integer.parseInt(rechargeRequest.getCharset()));
            }
            titanOrderPayreq.setExecutionrate(null);
            titanOrderPayreq.setExpand(rechargeRequest.getExpand());
            titanOrderPayreq.setExpand2(rechargeRequest.getExpand2());
            titanOrderPayreq.setMerchantNo(rechargeRequest.getUserid());
            titanOrderPayreq.setNotifyUrl(rechargeRequest.getNotifyUrl());
            if (rechargeRequest.getOrderAmount() != null) {
                titanOrderPayreq.setOrderAmount(Double.parseDouble(rechargeRequest.getOrderAmount()));
            }
            if (rechargeRequest.getOrderExpireTime() != null) {
                titanOrderPayreq.setOrderExpireTime(rechargeRequest.getOrderExpireTime());
            }
            if (rechargeRequest.getOrderMark() != null) {
                titanOrderPayreq.setOrderMark(Integer.parseInt(rechargeRequest.getOrderMark()));
            }
            titanOrderPayreq.setOrderNo(rechargeRequest.getOrderNo());
            titanOrderPayreq.setOrderTime(rechargeRequest.getOrderTime());

            titanOrderPayreq.setPageUrl(rechargeRequest.getPageUrl());
            titanOrderPayreq.setPayerAcount(rechargeRequest.getPayerAcount());
            titanOrderPayreq.setPayerMail(rechargeRequest.getPayerMail());

            titanOrderPayreq.setPayerName(rechargeRequest.getPayerName());
            titanOrderPayreq.setPayerPhone(rechargeRequest.getPayerPhone());
            titanOrderPayreq.setPayType(rechargeRequest.getPayType());
            titanOrderPayreq.setProductDesc(rechargeRequest.getProductDesc());
            titanOrderPayreq.setProductName(rechargeRequest.getProductName());

            titanOrderPayreq.setProductNo(rechargeRequest.getProductNo());
            if (rechargeRequest.getProductNum() != null) {
                titanOrderPayreq.setProductNum(Integer.parseInt(rechargeRequest.getProductNum()));
            }
            titanOrderPayreq.setRatetype(null);
            titanOrderPayreq.setReceivablefee(null);
            titanOrderPayreq.setReceivedfee(null);
            //本地秘钥加密还是
            String sign = getPaySigStr(rechargeRequest);
            if (sign != null) {
                titanOrderPayreq.setSignMsg(MD5.MD5Encode(sign, "UTF-8"));
            }

            if (rechargeRequest.getSignType() != null) {
                titanOrderPayreq.setSignType(Integer.parseInt(rechargeRequest.getSignType()));
            }
            titanOrderPayreq.setStandardrate(null);
            titanOrderPayreq.setVersion(rechargeRequest.getVersion());

        } catch (Exception e) {
            throw new Exception(e);
        }
        return titanOrderPayreq;
    }

//    /**
//     * 根据财务单号 查询系统生成的单号
//     * @return
//     * @throws Exception
//     */
//    private OrderIdResponse getOrderid() throws Exception {
//        OrderIdResponse orderIdResponse = new OrderIdResponse();
//        orderIdResponse.setOrderNo(OrderGenerateService.genSyncUserOrderId());
//        return orderIdResponse;
//    }


    /**
     * 对充值信息进行加密
     *
     * @param rechargeRequest
     * @return
     * @author fangdaikang
     */
    private String getPaySigStr(RechargeRequest rechargeRequest) {
        StringBuffer sign = new StringBuffer();
        if (rechargeRequest != null) {
            sign.append("signType=");
            sign.append(rechargeRequest.getSignType());
            sign.append("&version=");
            sign.append(rechargeRequest.getVersion());
            sign.append("&merchantNo=");
            sign.append(rechargeRequest.getMerchantNo());
            sign.append("&orderNo=");
            sign.append(rechargeRequest.getOrderNo());
            sign.append("&orderTime=");
            sign.append(rechargeRequest.getOrderTime());
            sign.append("&key=");
            sign.append(rechargeRequest.getKey());
            sign.append(")(*");
        }
        return sign.toString();
    }


    private FinanceSearchRemote getFinanceSearchRemote() {
        if (financeSearchRemote == null) {
            financeSearchRemote = hessianProxyBeanFactory.getHessianProxyBean(FinanceSearchRemote.class,
                    ProxyFactoryConstants.financeSearchRemoteUrl + "financeSearchRemote");
        }
        return financeSearchRemote;
    }
    
    private FinanceOrderRemote getFinanceOrderRemote(){
    	  if (financeOrderRemote == null) {
    		  financeOrderRemote = hessianProxyBeanFactory.getHessianProxyBean(FinanceOrderRemote.class,
                      ProxyFactoryConstants.financeSearchRemoteUrl + "financeOrderRemote");
          }
          return financeOrderRemote;
    }
    
    private HotelOrderSearchFacade getHotelOrderSearchFacade(){
    	if(hotelOrderSearchFacade==null){
    		hotelOrderSearchFacade  = hessianProxyBeanFactory.getHessianProxyBean(
    				HotelOrderSearchFacade.class,
    	            ProxyFactoryConstants.orderServiceUrl + "hotelOrderSearchFacade");
    	}
    	return hotelOrderSearchFacade;
    }

    private long getExpireTime(TitanOrderPayreq titanOrderPayreq){
        Integer orderExpireTime = titanOrderPayreq.getOrderExpireTime();
        if (orderExpireTime != null && orderExpireTime > 0 ) {
            return DateUtil.getSeconds(orderExpireTime);
        } else {
           return DateUtil.getDaySeconds(CommonConstant.ORDER_EXPIRE_TIME);
        }
    }

	@Override
	public AllowNoPwdPayResponse saveAllowNoPwdPay(
			AllowNoPwdPayRequest allowNoPwdPayRequest){ 
		AllowNoPwdPayResponse allowNoPwdPayResponse = new AllowNoPwdPayResponse();
		try{
			if(allowNoPwdPayRequest !=null){
				 TitanAccount titanAccount = new TitanAccount();
				 titanAccount.setAllownopwdpay(Integer.parseInt(allowNoPwdPayRequest.getStatus()));
				 titanAccount.setUserid(allowNoPwdPayRequest.getUserid());
				 titanAccountDao.update(titanAccount);
				 allowNoPwdPayResponse.putSuccess();
					 allowNoPwdPayResponse.putSysError();
				 }
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
		return allowNoPwdPayResponse;
	}

	@Override
	public AllowNoPwdPayResponse isAllowNoPwdPay(
			JudgeAllowNoPwdPayRequest judgeAllowNoPwdPayRequest) {
		AllowNoPwdPayResponse allowNoPwdPayResponse = new AllowNoPwdPayResponse();
		if(judgeAllowNoPwdPayRequest !=null && StringUtil.isValidString(judgeAllowNoPwdPayRequest.getMoney())){
			TitanAccountParam titanAccountParam = new TitanAccountParam();
			PaginationSupport<TitanAccount> paginationSupport = new PaginationSupport<TitanAccount>();
			titanAccountParam.setUserid(judgeAllowNoPwdPayRequest.getUserid());
			titanAccountParam.setAllownopwdpay(1);
			titanAccountDao.selectForPage(titanAccountParam, paginationSupport);
			if(paginationSupport.getItemList()!=null && paginationSupport.getItemList().size()>0){
				TitanAccount titanAccount = paginationSupport.getItemList().get(0);
				if(titanAccount!=null && titanAccount.getNopwdpaylimit()!=null){
					//？？有待讨论，和融数的整个数据交互式以分为单位，建议数据库不要使用double等数据形式
					BigDecimal allowNoPwdPay = new BigDecimal(titanAccount.getNopwdpaylimit());
					BigDecimal money = new BigDecimal(NumberUtil.covertToCents(judgeAllowNoPwdPayRequest.getMoney()));
					allowNoPwdPayResponse.setAllowNoPwdPay(false);
					if(allowNoPwdPay.subtract(money).doubleValue()>=0){
						allowNoPwdPayResponse.setAllowNoPwdPay(true);
					}
					allowNoPwdPayResponse.putSuccess();
					return allowNoPwdPayResponse;
				}
			}
		}
		allowNoPwdPayResponse.setAllowNoPwdPay(false);
		allowNoPwdPayResponse.putSysError();
		return allowNoPwdPayResponse;
	}

	@Override
	public RechargeResponse generateOrderNoAndRechargePage(
			RechargePageRequest rechargePageRequest) throws Exception {
		RechargeResponse rechargeResponse = new RechargeResponse();
		if(rechargePageRequest !=null){//落单操作
			OrderRequest orderRequest = new OrderRequest();
			if(StringUtil.isValidString(rechargePageRequest.getRechargeAmount())){
				orderRequest.setAmount(NumberUtil.covertToCents(rechargePageRequest.getRechargeAmount()));
			}
			orderRequest.setCreator(rechargePageRequest.getCreator());
			orderRequest.setGoodsname("充值");
			if(rechargePageRequest.getOperType() !=null){
				orderRequest.setOpertype(rechargePageRequest.getOperType().getKey());
			}
			if(rechargePageRequest.getOrdertype() !=null){
				orderRequest.setOrdertypeid(rechargePageRequest.getOrdertype().getKey());
			}
			orderRequest.setUserid(rechargePageRequest.getUserid());
			orderRequest.setUserorderid(rechargePageRequest.getUserorderid());
			//充值的订单和日期
			orderRequest.setOrderdate(new Date());
			orderRequest.setOrdertime(new Date());
			//自己的账户是收款账户
			orderRequest.setPayeemerchant(rechargePageRequest.getUserid());
			
			TransOrderCreateResponse transOrderCreateResponse =  operateRSTransOrder(orderRequest);
			if(transOrderCreateResponse !=null){
				if(StringUtil.isValidString(transOrderCreateResponse.getOrderNo())){//充值操作
					//将下单的融数的单号获取
					rechargePageRequest.setOrderid(transOrderCreateResponse.getOrderNo());
					RechargeRequest rechargeRequest = convertorToRechargeRequest(rechargePageRequest);
					RSPayOrderRequest rsPayOrderRequest = convertorToRSPayOrderRequest(rechargeRequest);
					RSPayOrderResponse response = rsPayOrderManager.getPayPage(rsPayOrderRequest);
					if(response !=null){
						if(CommonConstant.OPERATE_SUCCESS.equals(response.getOperateStatus())){
							//插入充值记录 根据orderNo拿单,记录充值单
							try{
								 TitanOrderPayreq titanOrderPayreq = convertorToTitanOrderPayReq(rechargeRequest);
					             if(rechargeRequest.getTransorderid()!=null){
					                titanOrderPayreq.setTransorderid(rechargeRequest.getTransorderid());
						            titanOrderPayreq.setReqstatus(ReqstatusEnum.Status_1.getStatus());
			                        titanOrderPayreqDao.insert(titanOrderPayreq);
					             }
							}catch(Exception e){
								log.error("手动充值单本地保存失败"+e.getMessage(),e);
							}
			              
							RechargeDataDTO rechargeDataDTO = new RechargeDataDTO();
							MyBeanUtil.copyProperties(rechargeDataDTO, response.getRsPayOrderRequest());
							rechargeResponse.setRechargeDataDTO(rechargeDataDTO); 
							rechargeResponse.putSuccess();
						}else{
							rechargeResponse.putErrorResult(response.getReturnCode(), response.getReturnMsg());
						}
						return rechargeResponse;
					}
				}
			}
		}
		rechargeResponse.putSysError();
		return rechargeResponse;
	}

	@Override
	public TradeDetailResponse getTradeDetail(TradeDetailRequest tradeDetailRequest) {
        TradeDetailResponse tradeDetailResponse = new TradeDetailResponse();
        tradeDetailResponse.setTransOrders(new PaginationSupport<TransOrderDTO>());
        try {
            TitanTransOrderParam condition = new TitanTransOrderParam();
            MyBeanUtil.copyProperties(condition, tradeDetailRequest);
            if (null != tradeDetailRequest.getTradeTypeEnum()) {
                condition.setStatus(tradeDetailRequest.getTradeTypeEnum().getKey());
            }
            if (StringUtil.isValidString(tradeDetailRequest.getOrderAmount())) {
                condition.setTradeamount(Long.valueOf(NumberUtil.covertToCents(tradeDetailRequest.getOrderAmount())));
            }
            if (StringUtil.isValidString(tradeDetailRequest.getOrderOperator())) {
                condition.setCreator(tradeDetailRequest.getOrderOperator());
            }
            if (StringUtil.isValidString(tradeDetailRequest.getAdmissionName())) {
                condition.setPartner(tradeDetailRequest.getAdmissionName());
            }
            if (StringUtil.isValidString(tradeDetailRequest.getIsEscrowedPayment())){
                condition.setIsEscrowedPayment(tradeDetailRequest.getIsEscrowedPayment());
            }
            if (StringUtil.isValidString(tradeDetailRequest.getUserOrderId())){
                condition.setUserorderid(tradeDetailRequest.getUserOrderId());
            }
            PaginationSupport<TitanTransOrder> pgSupport = new PaginationSupport<TitanTransOrder>();
            pgSupport.setPageSize(tradeDetailRequest.getPageSize());
            pgSupport.setCurrentPage(tradeDetailRequest.getCurrentPage());
            titanTransOrderDao.selectOrderForPage(condition, pgSupport);
            this.initTradeDetailResp(tradeDetailResponse, pgSupport);
            if (CollectionUtils.isNotEmpty(pgSupport.getItemList())) {//查询结果不为空。为空不能算出错
                for (TitanTransOrder titanTransOrder : pgSupport.getItemList()) {
                    TransOrderDTO transOrderDTO = new TransOrderDTO();
                    MyBeanUtil.copyProperties(transOrderDTO, titanTransOrder);
                    if (!StringUtil.isValidString(transOrderDTO.getUserid())) { //userId不合法
                        tradeDetailResponse.putErrorResult("USERID_INVALID", "查询结果中userId不合法");
                        return tradeDetailResponse;
                    }
                    if (isPayeeValid(transOrderDTO)) { //收款方存在时
                        if (isPayerValid(transOrderDTO)) {//付款方也存在
                            if (isPayeeOrg(tradeDetailRequest, transOrderDTO)) { //当前机构等于收款方
                                transOrderDTO.setTradeType("收款");
                                transOrderDTO.setTransTarget(getTransTarget(transOrderDTO.getPayermerchant()));//付款方
                            } else if (isPayerOrg(tradeDetailRequest, transOrderDTO)) {//当前机构等于付款方
                                transOrderDTO.setTradeType("付款");
                                transOrderDTO.setTransTarget(getTransTarget(transOrderDTO.getPayeemerchant()));//收款方
                            }
                        } else if (isPayeeOrg(tradeDetailRequest, transOrderDTO)) {//付款方不存在机构等于收款方
                            transOrderDTO.setTradeType("充值");
                            transOrderDTO.setTransTarget(getTransTarget(transOrderDTO.getPayeemerchant()));
                        }
                    } else if (!isPayeeValid(transOrderDTO)) {//收款方不存在
                        if (isPayerValid(transOrderDTO) && isPayerOrg(tradeDetailRequest, transOrderDTO)) {//付款方存在，机构等于付款方
                            transOrderDTO.setTransTarget(getTransTarget(transOrderDTO.getPayermerchant()));
                            transOrderDTO.setTradeType("提现");
                        }
                    }
                    tradeDetailResponse.getTransOrders().getItemList().add(transOrderDTO);
                }
                tradeDetailResponse.putSuccess();
                return tradeDetailResponse;
            }
        } catch (Exception e) {
            tradeDetailResponse.putSysError();
            log.error("查询交易明细失败" + e.getMessage(), e);
        }
        return tradeDetailResponse;
    }

	@Override
	public TradeDetailResponse getOrderTradeDetail(TradeDetailRequest tradeDetailRequest) {
		TradeDetailResponse tradeDetailResponse = new TradeDetailResponse();
        if(tradeDetailRequest == null || (!StringUtil.isValidString(tradeDetailRequest.getBusinessordercode()) &&
                !StringUtil.isValidString(tradeDetailRequest.getUserOrderId()))){
            tradeDetailResponse.putSysError();
            return tradeDetailResponse;
        }
		try{
            TradeDetailResponse tradeDetail = getTradeDetail(tradeDetailRequest);
			if(CollectionUtils.isNotEmpty(tradeDetail.getTransOrders().getItemList())){
				for(TransOrderDTO transOrderDTO :tradeDetail.getTransOrders().getItemList()){
					//获取充值记录
					if(transOrderDTO.getTradeType().equals("收款")){//收款记录
						TitanTransferDTO titanTransferDTO = new TitanTransferDTO();
						titanTransferDTO.setTransorderid(transOrderDTO.getTransid());
						titanTransferDTO = titanOrderService.getTitanTransferDTO(titanTransferDTO);
						transOrderDTO.setTitanTransferDTO(titanTransferDTO);
					}else if(transOrderDTO.getTradeType().equals("付款")){//付款记录
						TitanTransferDTO titanTransferDTO = new TitanTransferDTO();
						titanTransferDTO.setTransorderid(transOrderDTO.getTransid());
						titanTransferDTO = titanOrderService.getTitanTransferDTO(titanTransferDTO);

						TitanOrderPayDTO titanOrderPayDTO = new TitanOrderPayDTO();
						titanOrderPayDTO.setTransorderid(transOrderDTO.getTransid());
						titanOrderPayDTO = titanOrderService.getTitanOrderPayDTO(titanOrderPayDTO);

						transOrderDTO.setTitanOrderPayDTO(titanOrderPayDTO);
						transOrderDTO.setTitanTransferDTO(titanTransferDTO);

					}else if(transOrderDTO.getTradeType().equals("充值")){//获取提现记录
						TitanOrderPayDTO titanOrderPayDTO = new TitanOrderPayDTO();
						titanOrderPayDTO.setTransorderid(transOrderDTO.getTransid());
						titanOrderPayDTO = titanOrderService.getTitanOrderPayDTO(titanOrderPayDTO);
                        //设置银行名称
                        if (StringUtil.isValidString(titanOrderPayDTO.getBankInfo())) {
                            CashierItemBankDTO cashierItemBankDTO = titanCashierDeskService.queryCashierItemBankDTOByBankName(titanOrderPayDTO.getBankInfo());
                            if (cashierItemBankDTO != null) {
                                transOrderDTO.setBankname(cashierItemBankDTO.getBankMark());
                            }
                        }
						transOrderDTO.setTitanOrderPayDTO(titanOrderPayDTO);
					}else if(transOrderDTO.getTradeType().equals("提现")){//提现记录
						TitanWithDrawDTO titanWithDrawDTO = new TitanWithDrawDTO();
						titanWithDrawDTO.setTransorderid(transOrderDTO.getTransid());
						titanWithDrawDTO = titanOrderService.getTitanWithDrawDTO(titanWithDrawDTO);
						transOrderDTO.setTitanWithDrawDTO(titanWithDrawDTO);
			     	}
			    }
				tradeDetailResponse =  tradeDetail;
				tradeDetailResponse.putSuccess();
				return tradeDetailResponse;
			}
		}catch(Exception e){
			log.error("查询单个订单交易记录失败" + e.getMessage(),e);
		}
		tradeDetailResponse.putSysError();
		return tradeDetailResponse;
	}

	@Override
	public PaymentUrlResponse getPaymentUrl(PaymentUrlRequest paymentUrlRequest) {
		PaymentUrlResponse paymentUrlResponse =new PaymentUrlResponse();
		try{
			if(paymentUrlRequest !=null){
				if(StringUtil.isValidString(paymentUrlRequest.getPayOrderNo())){
					//生成一对秘钥
					String key = MD5.getSalt();
					//保存加密私钥
					TitanDynamicKey titanDynamicKey = new TitanDynamicKey();
					titanDynamicKey.setEncryptionkey(key);
					titanDynamicKey.setPayorderno(paymentUrlRequest.getPayOrderNo());
					titanDynamicKeyDao.delete(titanDynamicKey);
					titanDynamicKeyDao.insert(titanDynamicKey);
					
					StringBuffer url = getSignStr(paymentUrlRequest);
					url.insert(0,CommonConstant.REQUSET_URL); 
					StringBuffer sign = getSignStr(paymentUrlRequest);
					sign.append("&key="+key);
					url.append("&sign="+MD5.MD5Encode(sign.toString(), "UTF-8"));
					paymentUrlResponse.putSuccess();
					paymentUrlResponse.setUrl(url.toString());
					return paymentUrlResponse;
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
		paymentUrlResponse.putSysError();
		return paymentUrlResponse;
	}
	
	private StringBuffer getSignStr(PaymentUrlRequest paymentUrlRequest){
		StringBuffer sign = new StringBuffer();
		if(StringUtil.isValidString(paymentUrlRequest.getMerchantcode())){
			sign.append("?merchantcode="+paymentUrlRequest.getMerchantcode());
		}else{
			sign.append("?merchantcode="+"");
		}
		if(StringUtil.isValidString(paymentUrlRequest.getFcUserid())){
			sign.append("&fcUserid="+paymentUrlRequest.getFcUserid());
		}else{
			sign.append("&fcUserid="+"");
		}
		sign.append("&payOrderNo="+paymentUrlRequest.getPayOrderNo());
		sign.append("&paySource="+paymentUrlRequest.getPaySource());
		sign.append("&operater="+paymentUrlRequest.getOperater());
		
		if(StringUtil.isValidString(paymentUrlRequest.getRecieveMerchantCode())){
			sign.append("&recieveMerchantCode="+paymentUrlRequest.getRecieveMerchantCode());
		}else{
			sign.append("&recieveMerchantCode="+"");
		}
		sign.append("&isEscrowed="+paymentUrlRequest.getIsEscrowed());
		if(StringUtil.isValidString(paymentUrlRequest.getEscrowedDate())){
			sign.append("&escrowedDate="+paymentUrlRequest.getEscrowedDate());
		}else{
			sign.append("&escrowedDate="+"");
		}
		return sign;
	}

	@Override
	public LocalAddTransOrderResponse addLocalTransOrder(
			PaymentRequest paymentRequest,
			FinancialOrderResponse financialOrderResponse) {
		LocalAddTransOrderResponse localAddTransOrderResponse = new LocalAddTransOrderResponse();
		try{
			//判断该支付是否已经下单，处理中的就返回单号，失败返回新单号，成功就拒绝下单
			TransOrderResponse transOrderResponse = queryTransOrderByCode(paymentRequest.getPayOrderNo());
			if(transOrderResponse !=null && transOrderResponse.getTransOrder() !=null){
				TransOrderDTO transOrderDTO = transOrderResponse.getTransOrder();

				if(OrderStatusEnum.Status_1.getStatus().equals(transOrderDTO.getStatusid())||
						OrderStatusEnum.Status_4.getStatus().equals(transOrderDTO.getStatusid())){
					//两次需要充值的金额不等需改订单
					boolean flag = validateIsUpdateOrder(paymentRequest,transOrderDTO);
					if(flag){//充值金额相等则返回该单号，否则生成订单
						localAddTransOrderResponse.setTransid(transOrderDTO.getTransid());
						localAddTransOrderResponse.setOrderNo(transOrderDTO.getOrderid());
						localAddTransOrderResponse.putSuccess();
						return localAddTransOrderResponse;
					}
					
				}else if(OrderStatusEnum.Status_2.getStatus().equals(transOrderDTO.getStatusid())||
						OrderStatusEnum.Status_3.getStatus().equals(transOrderDTO.getStatusid())){
					localAddTransOrderResponse.putErrorResult("已支付，请勿重复支付");
					//再次回调财务
					confirmFinance(transOrderDTO);
					return localAddTransOrderResponse;
				}
			}
			//未下单
			OrderRequest orderRequest = convertorToTitanOrderRequest(paymentRequest);
			TitanTransOrder titanTransOrder = orderRequest2TitanTransOrder(orderRequest);
			titanTransOrder.setOrderid(OrderGenerateService.genLocalOrderNo());
			//融数下单
			
			if(titanTransOrderDao.insert(titanTransOrder)>0?true:false){
            	 localAddTransOrderResponse.setTransid(titanTransOrder.getTransid());
            	 localAddTransOrderResponse.setOrderNo(titanTransOrder.getOrderid());
            	 localAddTransOrderResponse.putSuccess();
             }
            
		    //本地落单
		}catch(Exception e){
			log.error("融数支付本地落单失败"+e.getMessage(),e);
			localAddTransOrderResponse.putSysError();
		}
		return localAddTransOrderResponse;
	}
	
	//验证是否需要修改订单
	private boolean validateIsUpdateOrder(PaymentRequest paymentRequest,TransOrderDTO transOrderDTO) throws Exception{
		if(!NumberUtil.covertToCents(paymentRequest.getPayAmount()).equals(transOrderDTO.getAmount().toString())){
			//直接将本地单作废重新生成新订单
			return updateLocalOrder(transOrderDTO.getTransid());
		}
		return true;
	}
	
	//修改本地落单
	private boolean updateLocalOrder(Integer transid) throws Exception{
		TitanTransOrder titanTransOrder = new TitanTransOrder();
		try{
			titanTransOrder.setStatusid(OrderStatusEnum.Status_5.getStatus());
			titanTransOrder.setTransid(transid);
			int row = titanTransOrderDao.update(titanTransOrder);
			if(row>0){
				return true;
			}
		}catch(Exception e){
			log.error("修改本地订单失败");
		}
		return false;
	}
	
	@Override
	public PayMethodConfigDTO getPayMethodConfigDTO(
			PayMethodConfigRequest payMethodConfigRequest) {
		try{
			if(payMethodConfigRequest !=null && StringUtil.isValidString(payMethodConfigRequest.getUserId())){
				List<TitanPayMethodConfig> titanPayMethodConfigList = titanPayMethodDao.queryTitanPayMethod(payMethodConfigRequest);
		        if(titanPayMethodConfigList.size() ==1){
		        	PayMethodConfigDTO payMethodConfigDTO = new PayMethodConfigDTO();
		        	MyBeanUtil.copyProperties(payMethodConfigDTO, titanPayMethodConfigList.get(0));
		        	return payMethodConfigDTO;
		        }else if(titanPayMethodConfigList.size()>1){
		        	log.error("查询支付方式的数据重复");
		        }
			}
		}catch(Exception e){
			log.error("查询支付方式的配置出错"+e.getMessage(),e);
		}
		
		return null;
	}

    @Override
    public TransOrderUpdateResponse updateTransOrder(TransOrderUpdateRequest transOrderUpdateRequest) {
        TransOrderUpdateResponse updateResponse = new TransOrderUpdateResponse();
        if (!GenericValidate.validate(transOrderUpdateRequest)){
            log.error("参数校验错误");
            updateResponse.putErrorResult("参数校验错误");
            return updateResponse;
        }
        TitanTransOrder transOrder = new TitanTransOrder();
        transOrder.setUserid(transOrderUpdateRequest.getUserId());
        transOrder.setUserorderid(transOrderUpdateRequest.getUserOrderId());
        transOrder.setRemark(transOrderUpdateRequest.getRemark());
        try {
            titanTransOrderDao.update(transOrder);
        } catch (Exception e) {
            log.error("交易单更新失败", e);
            updateResponse.putErrorResult("交易单更新失败");
            return updateResponse;
        }
        updateResponse.putSuccess();
        return updateResponse;
    }
	
    private void initTradeDetailResp(TradeDetailResponse tradeDetailResponse, PaginationSupport<TitanTransOrder> pgSupport){
        tradeDetailResponse.getTransOrders().setPageSize(pgSupport.getPageSize());
        tradeDetailResponse.getTransOrders().setCurrentPage(pgSupport.getCurrentPage());
        tradeDetailResponse.getTransOrders().setOrderBy(pgSupport.getOrderBy());
        tradeDetailResponse.getTransOrders().setTotalCount(pgSupport.getTotalCount());
        tradeDetailResponse.getTransOrders().setTotalPage(pgSupport.getTotalPage());
        tradeDetailResponse.getTransOrders().setItemList(new ArrayList<TransOrderDTO>());
    }

    private boolean isPayerValid(TransOrderDTO transOrderDTO){
        return StringUtil.isValidString(transOrderDTO.getPayermerchant());
    }

    private boolean isPayeeValid(TransOrderDTO transOrderDTO){
        return StringUtil.isValidString(transOrderDTO.getPayeemerchant());
    }

    private boolean isPayerOrg(TradeDetailRequest tradeDetailRequest,TransOrderDTO transOrderDTO){
        return tradeDetailRequest.getUserid().equals(transOrderDTO.getPayermerchant());
    }

    private boolean isPayeeOrg(TradeDetailRequest tradeDetailRequest,TransOrderDTO transOrderDTO){
        return tradeDetailRequest.getUserid().equals(transOrderDTO.getPayeemerchant());
    }

    //查询收款机构名称
    private String getTransTarget(String orgUserId){
        String result = "";
        FinancialOrganQueryRequest organQueryRequest = new FinancialOrganQueryRequest();
        organQueryRequest.setUserId(orgUserId);
        FinancialOrganResponse organResponse = titanFinancialOrganService.queryFinancialOrgan(organQueryRequest);
        if (organResponse.isResult() && organResponse.getFinancialOrganDTO() != null) {
            result = organResponse.getFinancialOrganDTO().getOrgName();
            if (null != organResponse.getFinancialOrganDTO().getOrgBindInfo() &&
                    StringUtil.isValidString(organResponse.getFinancialOrganDTO().getOrgBindInfo().getMerchantName())){
                result = result + "-" + organResponse.getFinancialOrganDTO().getOrgBindInfo().getMerchantName();
            }
        }
        return result;
    }

	@Override
	public GDPOrderResponse getGDPOrderDTO(String orderCode) {
		log.info("GDP查询入参:"+JSON.toJSONString(orderCode));
		GDPOrderResponse gDPOrderResponse = new GDPOrderResponse();
		OrderDetailResponseDTO orderDetailResponseDTO = this.getHotelOrderSearchFacade().queryOrderByCode(orderCode);
	    log.info("GDP查询的结果:"+JSON.toJSONString(orderDetailResponseDTO));
		if(orderDetailResponseDTO !=null){
	    	GDPOrderDTO gDPOrderDTO = new GDPOrderDTO();
	    	if(orderDetailResponseDTO.getCurrency()!=null){
	    		gDPOrderDTO.setCurrency(orderDetailResponseDTO.getCurrency().getValue());
	    	}
	    	gDPOrderDTO.setHotelName(orderDetailResponseDTO.getHotelName());
	    	gDPOrderDTO.setOrderSum(orderDetailResponseDTO.getOrderSum());
	    	gDPOrderDTO.setGoodName(orderDetailResponseDTO.getCommondityName());
	    	gDPOrderDTO.setOrderCode(orderDetailResponseDTO.getOrderCode());
	    	gDPOrderResponse.setgDPOrderDTO(gDPOrderDTO);
	    	gDPOrderResponse.putSuccess();
	    	return gDPOrderResponse;
	    }
	    gDPOrderResponse.putSysError();
		return gDPOrderResponse;
	}
	
	private void lockOutTradeNoList(String out_trade_no) throws InterruptedException {
		synchronized (orderNoList) {
			while(orderNoList.contains(out_trade_no)) {
				orderNoList.wait();
			}
			orderNoList.add(out_trade_no);
		} 
	}
	
	private void unlockOutTradeNoList(String out_trade_no) {
		synchronized (orderNoList) {
			orderNoList.remove(out_trade_no);
			orderNoList.notifyAll();
		}
	}
	
	
}
