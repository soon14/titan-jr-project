package com.fangcang.titanjr.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.fangcang.titanjr.dto.PaySourceEnum;
import com.fangcang.titanjr.dto.request.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fangcang.corenut.dao.PaginationSupport;
import com.fangcang.titanjr.common.bean.ValidateResponse;
import com.fangcang.titanjr.common.enums.CashierItemTypeEnum;
import com.fangcang.titanjr.common.enums.SupportBankEnum;
import com.fangcang.titanjr.common.util.CommonConstant;
import com.fangcang.titanjr.common.util.GenericValidate;
import com.fangcang.titanjr.dao.CommonPayHistoryDao;
import com.fangcang.titanjr.dao.TitanCashierDeskDao;
import com.fangcang.titanjr.dao.TitanCashierDeskItemDao;
import com.fangcang.titanjr.dao.TitanCashierItemBankDao;
import com.fangcang.titanjr.dao.TitanCommonPayMethodDao;
import com.fangcang.titanjr.dao.TitanOrgDao;
import com.fangcang.titanjr.dao.TitanRateConfigDao;
import com.fangcang.titanjr.dto.BaseResponseDTO;
import com.fangcang.titanjr.dto.bean.CashierDeskDTO;
import com.fangcang.titanjr.dto.bean.CashierItemBankDTO;
import com.fangcang.titanjr.dto.bean.CommonPayMethodDTO;
import com.fangcang.titanjr.dto.bean.ItemBankDTO;
import com.fangcang.titanjr.dto.bean.gateway.CommonPayHistoryDTO;
import com.fangcang.titanjr.dto.response.CashierDeskInitResponse;
import com.fangcang.titanjr.dto.response.CashierDeskResponse;
import com.fangcang.titanjr.entity.TitanCashierDesk;
import com.fangcang.titanjr.entity.TitanCashierDeskItem;
import com.fangcang.titanjr.entity.TitanCashierItemBank;
import com.fangcang.titanjr.entity.TitanCommonPayMethod;
import com.fangcang.titanjr.entity.TitanOrg;
import com.fangcang.titanjr.entity.TitanRateConfig;
import com.fangcang.titanjr.entity.parameter.TitanCommonPayMethodParam;
import com.fangcang.titanjr.service.TitanCashierDeskService;
import com.fangcang.titanjr.service.TitanFinancialOrganService;
import com.fangcang.util.DateUtil;
import com.fangcang.util.MyBeanUtil;
import com.fangcang.util.StringUtil;

/**
 * Created by zhaoshan on 2016/5/18.
 */
@Service("titanCashierDeskService")
public class TitanCashierDeskServiceImpl implements TitanCashierDeskService, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6036678797022327373L;

    private static final Log log = LogFactory.getLog(TitanCashierDeskServiceImpl.class);

    @Resource
    TitanCashierDeskDao titanCashierDeskDao;

    @Resource
    TitanCashierDeskItemDao titanCashierDeskItemDao;

    @Resource
    TitanCashierItemBankDao titanCashierItemBankDao;

    @Resource
    TitanCommonPayMethodDao titanCommonPayMethodDao;

    @Resource
    TitanRateConfigDao titanRateConfigDao;

    @Resource
    TitanFinancialOrganService titanFinancialOrganService;

    @Resource
    TitanOrgDao titanOrgDao;

    @Resource
    CommonPayHistoryDao commonPayHistoryDao;

    @Override
    public CashierDeskResponse queryCashierDesk(CashierDeskQueryRequest cashierDeskQueryRequest) {
        //需要补充accountHistory，账户是否需要密码等数据
        CashierDeskResponse deskResponse = new CashierDeskResponse();
        try {
            List<CashierDeskDTO> result = titanCashierDeskDao.queryCashierDesk(cashierDeskQueryRequest);
            if (CollectionUtils.isNotEmpty(result)) {
                deskResponse.setCashierDeskDTOList(result);
            }
            deskResponse.putSuccess();
        } catch (Exception e) {
            log.error("查询收银台异常", e);
            deskResponse.putSysError();
        }
        return deskResponse;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public CashierDeskInitResponse initCashierDesk(CashierDeskInitRequest cashierDeskInitRequest) throws Exception {
        CashierDeskInitResponse deskInitResponse = new CashierDeskInitResponse();
        try {
            List<TitanCashierDesk> cashierDeskList = this.buildCashierDeskList(cashierDeskInitRequest, null);

            if (CollectionUtils.isNotEmpty(cashierDeskList)) {
                for (TitanCashierDesk cashierDesk : cashierDeskList) {
                    titanCashierDeskDao.saveCashierDesk(cashierDesk);
                }
            }

            //B2B的收银台有下面三个选项
            List<TitanCashierDeskItem> cashierDeskItems = new ArrayList<TitanCashierDeskItem>();
            if (CollectionUtils.isNotEmpty(cashierDeskList)) {
                for (TitanCashierDesk cashierDesk : cashierDeskList) {
                    //所有PC收银台类型都有B2B银行列表配置
                    if (cashierDesk.getUsedfor() != Integer.valueOf(PaySourceEnum.TT_MALL_MOBILE.getDeskCode()) &&
                            cashierDesk.getUsedfor() != Integer.valueOf(PaySourceEnum.TRADING_PLATFORM_MOBILE.getDeskCode()) &&
                            cashierDesk.getUsedfor() != Integer.valueOf(PaySourceEnum.DISTRIBUTION_MOBILE.getDeskCode())) {
                        cashierDeskItems.add(buildCashierDeskItem(cashierDesk.getDeskid(), CashierItemTypeEnum.B2B_ITEM));
                        cashierDeskItems.add(buildCashierDeskItem(cashierDesk.getDeskid(), CashierItemTypeEnum.B2C_ITEM));
                        //不等于充值时，都可以增加
                        if (cashierDesk.getUsedfor() != Integer.valueOf(PaySourceEnum.RECHARGE.getDeskCode())) {
                            cashierDeskItems.add(buildCashierDeskItem(cashierDesk.getDeskid(), CashierItemTypeEnum.CREDIT_ITEM));
                            cashierDeskItems.add(buildCashierDeskItem(cashierDesk.getDeskid(), CashierItemTypeEnum.QR_ITEM));
                            cashierDeskItems.add(buildCashierDeskItem(cashierDesk.getDeskid(), CashierItemTypeEnum.BALANCE_ITEM));
                        }
                        //只有等于财务时，可以加上贷款支付
                        if (cashierDesk.getUsedfor() == Integer.valueOf(PaySourceEnum.FINANCE_SUPPLY_PC.getDeskCode())) {
                            cashierDeskItems.add(buildCashierDeskItem(cashierDesk.getDeskid(), CashierItemTypeEnum.LOAN));
                        }
                    }
                }
            }

            //批量插入初始化收银台子项
            for (TitanCashierDeskItem deskItem : cashierDeskItems) {
                titanCashierDeskItemDao.saveCashierDeskItem(deskItem);
            }

            //默认初始化银行：
            List<TitanCashierItemBank> allItemBanks = new ArrayList<TitanCashierItemBank>();
            for (TitanCashierDeskItem deskItem : cashierDeskItems) {
                if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.B2B_ITEM.getItemCode())) {
                    allItemBanks.addAll(buildItemBankList(deskItem.getItemid(), "B2B"));
                }
                if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.B2C_ITEM.getItemCode())) {
                    allItemBanks.addAll(buildItemBankList(deskItem.getItemid(), "B2C"));
                }
                if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.CREDIT_ITEM.getItemCode())) {
                    allItemBanks.addAll(buildItemBankList(deskItem.getItemid(), "Credit"));
                }
                if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.QR_ITEM.getItemCode())) {
                    allItemBanks.addAll(buildQRBankList(deskItem.getItemid()));
                }
                if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.LOAN.getItemCode())) {
                    allItemBanks.addAll(buildLoanBankList(deskItem.getItemid()));
                }
            }

            titanCashierItemBankDao.batchSaveItemBanks(allItemBanks);

            //默认初始化费率设置
            List<TitanRateConfig> rateConfigList = new ArrayList<TitanRateConfig>();
            if (CollectionUtils.isNotEmpty(cashierDeskList)) {
                for (TitanCashierDesk cashierDesk : cashierDeskList) {
                    rateConfigList.add(buildPayRateConfig(cashierDesk, cashierDeskInitRequest.getUserId()));
                }
                //增加提现的费率
                rateConfigList.add(buildPayRateConfig(null, cashierDeskInitRequest.getUserId()));
            }

            titanRateConfigDao.batchSaveRateConfigs(rateConfigList);
            deskInitResponse.putSuccess();
        } catch (Exception e) {
            log.error("初始化收银台数据异常", e);
            deskInitResponse.putSysError();
        }
        return deskInitResponse;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public boolean updateCashierDesk(CashierDeskUpdateRequest deskUpdateRequest) throws Exception {
        if (!StringUtil.isValidString(deskUpdateRequest.getUserId()) &&
                null == deskUpdateRequest.getDeskId()) {
            log.error("收银台配置更新失败,参数不合法");
            return false;
        }
        try {
            titanCashierDeskDao.updateCashierDesk(deskUpdateRequest);
            return true;
        } catch (Exception e){
            log.error("收银台更新失败" ,e);
            throw e;
        }
    }

    private TitanRateConfig buildPayRateConfig(TitanCashierDesk titanCashierDesk, String userId) {
        TitanRateConfig rateConfig = new TitanRateConfig();
        rateConfig.setUserid(userId);
        if(titanCashierDesk == null){//提现没有实际的收银台，但是要配置费率
        	rateConfig.setDeskid("TX");
        	rateConfig.setUsedfor(PaySourceEnum.WITHDRAW_PC.getDeskCode());
        	rateConfig.setDescription("提现收银台");
            rateConfig.setRatetype(CommonConstant.RATETYPE_FIXATION);
            rateConfig.setStandrate(5f);
            rateConfig.setExecutionrate(0f);
        }else{
    		rateConfig.setDeskid(String.valueOf(titanCashierDesk.getDeskid()));
    		rateConfig.setUsedfor(String.valueOf(titanCashierDesk.getUsedfor()));
            rateConfig.setDescription(titanCashierDesk.getDeskname());
            rateConfig.setRatetype(CommonConstant.RATETYPE_PERCENT);
            rateConfig.setStandrate(0.4f);
            rateConfig.setExecutionrate(0f);
            if(titanCashierDesk.getUsedfor() != null){
        		if(titanCashierDesk.getUsedfor() == 5){
        			rateConfig.setStandrate(0f);//充值免费
        		}
        		if(titanCashierDesk.getUsedfor() == 6){
        			rateConfig.setExecutionrate(0.4f);//对外开放收银台收千分四
        		}
            }
        }
        rateConfig.setCreator("system");
        rateConfig.setCreatetime(new Date());
        rateConfig.setExpiration(DateUtil.getDate(new Date(), 6));//默认6个月

        return rateConfig;
    }

    private List<TitanCashierItemBank> buildItemBankList(Integer bankItemId, String type) {
        List<TitanCashierItemBank> result = new ArrayList<TitanCashierItemBank>();
        for (SupportBankEnum bankEnum : SupportBankEnum.values()) {
            if (bankEnum.bankType.equals(type)) {
                TitanCashierItemBank bank = new TitanCashierItemBank();
                bank.setItemid(bankItemId);
                bank.setBankmark(bankEnum.bankRemark);
                bank.setBankname(bankEnum.bankName);
                bank.setCreator("system");
                bank.setCreatetime(new Date());
                result.add(bank);
            }
        }
        return result;
    }

    private List<TitanCashierItemBank> buildLoanBankList(Integer bankItemId) {
        List<TitanCashierItemBank> result = new ArrayList<TitanCashierItemBank>();
        TitanCashierItemBank wx = new TitanCashierItemBank();
        wx.setItemid(bankItemId);
        wx.setBankmark("运营贷");
        wx.setBankname("yyd");
        wx.setCreator("system");
        wx.setCreatetime(new Date());
        result.add(wx);
        return result;
    }

    private List<TitanCashierItemBank> buildQRBankList(Integer bankItemId) {
        List<TitanCashierItemBank> result = new ArrayList<TitanCashierItemBank>();
        TitanCashierItemBank wx = new TitanCashierItemBank();
        wx.setItemid(bankItemId);
        wx.setBankmark("微信");
        wx.setBankname("wx");
        wx.setCreator("system");
        wx.setCreatetime(new Date());
        result.add(wx);
        TitanCashierItemBank alipay = new TitanCashierItemBank();
        alipay.setItemid(bankItemId);
        alipay.setBankmark("支付宝");
        alipay.setBankname("alipay");
        alipay.setCreator("system");
        alipay.setCreatetime(new Date());
        result.add(alipay);
        return result;
    }

    private List<TitanCashierDesk> buildCashierDeskList(CashierDeskInitRequest cashierDeskInitRequest, PaySourceEnum deskType) {
        List<TitanCashierDesk> deskList = new ArrayList<TitanCashierDesk>();
        if (null == deskType) {
            for (PaySourceEnum paySourceEnum : PaySourceEnum.values()) {
            	if(paySourceEnum != PaySourceEnum.WITHDRAW_PC){//提现没有收银台配置
            		deskList.add(getCashierDesk(cashierDeskInitRequest, paySourceEnum));
            	}
            }
        } else {
            deskList.add(getCashierDesk(cashierDeskInitRequest, deskType));
        }
        return deskList;
    }

    private TitanCashierDesk getCashierDesk(CashierDeskInitRequest initRequest,
                                            PaySourceEnum paySourceEnum) {
        TitanCashierDesk desk = new TitanCashierDesk();
        desk.setUserid(initRequest.getUserId());
        desk.setCreator(initRequest.getOperator());
        desk.setCreatetime(new Date());
        desk.setConstid(initRequest.getConstId());
        desk.setPaytype(1);
        desk.setUsedfor(Integer.valueOf(paySourceEnum.getDeskCode()));
        desk.setDeskname(paySourceEnum.getDeskName());
        return desk;
    }

    private TitanCashierDeskItem buildCashierDeskItem(Integer deskId, CashierItemTypeEnum deskItemType) {
        TitanCashierDeskItem deskItem = new TitanCashierDeskItem();
        deskItem.setDeskid(deskId);
        deskItem.setItemtype(Integer.valueOf(deskItemType.itemCode));
        deskItem.setItemname(deskItemType.itemName);
        deskItem.setCreator("system");
        deskItem.setCreatetime(new Date());
        return deskItem;
    }

    @Override
    public CashierItemBankDTO queryCashierItemBankDTOByBankName(String bankName) {
        try {
            List<TitanCashierItemBank> titanCashierItemBankList = titanCashierItemBankDao.queryCashierItemBankDTOByBankName(bankName);
            if (titanCashierItemBankList != null && titanCashierItemBankList.size() > 0) {
                return converToCashierItemBankDTO(titanCashierItemBankList.get(0));
            }

        } catch (Exception e) {
            log.error("查询银行信息失败" + e.getMessage(), e);
        }
        return null;
    }

    private CashierItemBankDTO converToCashierItemBankDTO(TitanCashierItemBank titanCashierItemBank) {
        if (titanCashierItemBank != null) {
            CashierItemBankDTO cashierItemBankDTO = new CashierItemBankDTO();
            cashierItemBankDTO.setBankImage(titanCashierItemBank.getBankimage());
            cashierItemBankDTO.setBankMark(titanCashierItemBank.getBankmark());
            cashierItemBankDTO.setBankName(cashierItemBankDTO.getBankName());
            return cashierItemBankDTO;
        }
        return null;
    }

    @Override
    public boolean saveCommonPayMethod(CommonPayMethodDTO commonPayMethodDTO) {
        if (commonPayMethodDTO != null && StringUtil.isValidString(commonPayMethodDTO.getBankname())
                && commonPayMethodDTO.getDeskid() != null
                && commonPayMethodDTO.getPaytype() != null) {
            //更新还是插入，根据deskId,bankName,payType判断
            TitanCommonPayMethodParam condition = new TitanCommonPayMethodParam();
            condition.setBankname(commonPayMethodDTO.getBankname());
            condition.setDeskid(commonPayMethodDTO.getDeskid());
            condition.setPaytype(commonPayMethodDTO.getPaytype());

            PaginationSupport<TitanCommonPayMethod> paginationSupport = new PaginationSupport<TitanCommonPayMethod>();
            titanCommonPayMethodDao.selectForPage(condition, paginationSupport);
            //判断该deskId,bankMark,payType只对应一条记录
            if (paginationSupport.getItemList() != null && paginationSupport.getItemList().size() != 0) {
                if (paginationSupport.getItemList().size() == 1) {
                    TitanCommonPayMethod titanCommonPayMethod = paginationSupport.getItemList().get(0);
                    if (paginationSupport.getItemList().get(0).getCount() != null) {
                        titanCommonPayMethod.setCount(titanCommonPayMethod.getCount() + 1);
                    }
                    return titanCommonPayMethodDao.update(titanCommonPayMethod) > 0 ? true : false;
                } else {
                    log.error("常用的的支付方式错误" + "deskId:" + commonPayMethodDTO.getDeskid());
                }
            } else {
                TitanCommonPayMethod titanCommonPayMethod = new TitanCommonPayMethod();
                MyBeanUtil.copyProperties(titanCommonPayMethod, commonPayMethodDTO);
                titanCommonPayMethod.setCount(1);
                return titanCommonPayMethodDao.insert(titanCommonPayMethod) > 0 ? true : false;
            }
        }
        return false;
    }

    @Override
    public List<CommonPayMethodDTO> queryCommonPayMethod(
            CashierDeskQueryRequest cashierDeskQueryRequest) {
        try {
            return titanCommonPayMethodDao.queryCommomPayMethod(cashierDeskQueryRequest);
        } catch (Exception e) {
            log.error("查询常用支付方式失败" + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public BaseResponseDTO saveCommonPayHistory(CommonPayHistoryDTO commonPayHistoryDTO) {

        BaseResponseDTO baseResponseDTO = new BaseResponseDTO();
        baseResponseDTO.putSuccess();
        if (!StringUtil.isValidString(commonPayHistoryDTO.getIdtype())) {
        	commonPayHistoryDTO.setIdtype("1");
        }

        ValidateResponse res = GenericValidate.validateObj(commonPayHistoryDTO);
        if (!res.isSuccess()) {
            log.error("saveQuickcardHistory error：" + res.getReturnMessage());
            baseResponseDTO.putErrorResult(res.getReturnMessage());
            return baseResponseDTO;
        }

        Map<String, String> condition = new HashMap<String, String>();
        condition.put("fcuserid", commonPayHistoryDTO.getFcuserid());
        condition.put("orgcode", commonPayHistoryDTO.getOrgcode());
        condition.put("deskid", commonPayHistoryDTO.getDeskid());
        condition.put("bankinfo", commonPayHistoryDTO.getBankinfo());
        condition.put("paytype", commonPayHistoryDTO.getPaytype());
        condition.put("payeracount", commonPayHistoryDTO.getPayeracount());
        PaginationSupport<CommonPayHistoryDTO> paginationSupport = new PaginationSupport<CommonPayHistoryDTO>();
        commonPayHistoryDao.selectForPage(condition, paginationSupport);

        if (paginationSupport.getItemList() != null && paginationSupport.getItemList().size() != 0) {
            if (paginationSupport.getItemList().size() == 1) {
                CommonPayHistoryDTO commonPayHistory = paginationSupport.getItemList().get(0);
                commonPayHistory.setCount(commonPayHistory.getCount() + 1);
                if (commonPayHistoryDao.update(commonPayHistory) <= 0) {
                    log.error("更新快捷支付卡历史失败");
                    baseResponseDTO.putSysError();
                }
                log.info("更新快捷支付卡历史记录成功");
            } else {
                log.error("快捷支付卡历史记录重复  fcuserid=" + commonPayHistoryDTO.getFcuserid() + ", payeracount=" + commonPayHistoryDTO.getPayeracount());
            }
        } else {

        	commonPayHistoryDTO.setCount(1);
            if (commonPayHistoryDao.insert(commonPayHistoryDTO) <= 0) {
                log.error("添加快捷支付卡历史记录失败");
                baseResponseDTO.putSysError();
            }else{
            	log.info("添加快捷支付卡历史记录成功");
            }
        }

        return baseResponseDTO;
    }

    @Override
    public List<CommonPayHistoryDTO> queryCommonPayHistory(CommonPayHistoryDTO commonPayHistoryDTO) {
        try {
            return commonPayHistoryDao.selectCommonPayHistory(commonPayHistoryDTO);
        } catch (Exception e) {
            log.error("查询快捷支付卡历史记录异常：", e);
        }
        return null;
    }

    @Override
    public void initttMallCashDesk() {
        //查询出所以的userID
        CashierDeskInitRequest cashierDeskInitRequest = new CashierDeskInitRequest();
        FinancialOrganQueryRequest organ = new FinancialOrganQueryRequest();
        organ.setStatusId(1);
        List<TitanOrg> orgList = titanOrgDao.queryTitanOrgList(organ);
        for (TitanOrg org : orgList) {
            cashierDeskInitRequest.setConstId(CommonConstant.RS_FANGCANG_CONST_ID);
            cashierDeskInitRequest.setUserId(org.getUserid());
            this.initDesk(cashierDeskInitRequest);
        }
    }

    private void initDesk(CashierDeskInitRequest cashierDeskInitRequest) {
        try {
            //首先添加CashDesk
            TitanCashierDesk ttMAllCashierDesk = this.buildCashierDeskList(cashierDeskInitRequest, PaySourceEnum.TT_MALL_PC).get(0);
            //批量插入初始化收银台
            titanCashierDeskDao.saveCashierDesk(ttMAllCashierDesk);
            TitanCashierDeskItem ttMall2bitem = buildCashierDeskItem(ttMAllCashierDesk.getDeskid(), CashierItemTypeEnum.B2B_ITEM);
            TitanCashierDeskItem ttMall2citem = buildCashierDeskItem(ttMAllCashierDesk.getDeskid(), CashierItemTypeEnum.B2C_ITEM);
            TitanCashierDeskItem ttMallCritem = buildCashierDeskItem(ttMAllCashierDesk.getDeskid(), CashierItemTypeEnum.CREDIT_ITEM);
            TitanCashierDeskItem ttMallQritem = buildCashierDeskItem(ttMAllCashierDesk.getDeskid(), CashierItemTypeEnum.QR_ITEM);
            //批量插入初始化收银台子项

            titanCashierDeskItemDao.saveCashierDeskItem(ttMall2bitem);
            titanCashierDeskItemDao.saveCashierDeskItem(ttMall2citem);
            titanCashierDeskItemDao.saveCashierDeskItem(ttMallCritem);
            titanCashierDeskItemDao.saveCashierDeskItem(ttMallQritem);

            //默认初始化银行：
            List<TitanCashierItemBank> allItemBanks = new ArrayList<TitanCashierItemBank>();
            allItemBanks.addAll(buildItemBankList(ttMall2bitem.getItemid(), "B2B"));
            allItemBanks.addAll(buildItemBankList(ttMall2citem.getItemid(), "B2C"));
            allItemBanks.addAll(buildItemBankList(ttMallCritem.getItemid(), "Credit"));
            allItemBanks.addAll(buildQRBankList(ttMallQritem.getItemid()));

            titanCashierItemBankDao.batchSaveItemBanks(allItemBanks);
        } catch (Exception e) {
            log.error("初始化收银台失败:" + cashierDeskInitRequest.getUserId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    @Override
    public void executeLoanDeskInit() throws Exception {
        try {
            List<CashierDeskDTO> cashierDeskDTOs = titanCashierDeskDao.queryNotAssociatedLoanCashierdesk();
            TitanCashierDeskItem loan = null;
            List<TitanCashierItemBank> allItemBanks = new ArrayList<TitanCashierItemBank>();
            for (CashierDeskDTO cashierDeskDTO : cashierDeskDTOs) {
                loan = buildCashierDeskItem(cashierDeskDTO.getDeskId(), CashierItemTypeEnum.LOAN);
                titanCashierDeskItemDao.saveCashierDeskItem(loan);
                allItemBanks.addAll(buildLoanBankList(loan.getItemid()));
            }
            titanCashierItemBankDao.batchSaveItemBanks(allItemBanks);
        } catch (Exception e) {
            log.error("execute loan desk fail!", e);
            throw e;
        }
    }


    /*@Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
    public void executeWxPublicInit() throws Exception {

        List<String> useridList = titanCashierDeskDao.queryNotExistCashierDesk(
                Integer.valueOf(PaySourceEnum.TT_MALL_MOBILE.getDeskCode()));

        CashierDeskInitRequest cashierDeskInitRequest = new CashierDeskInitRequest();
        //添加收银台
        for (String userid : useridList) {

            cashierDeskInitRequest.setUserId(userid);
            cashierDeskInitRequest.setConstId(CommonConstant.RS_FANGCANG_CONST_ID);
            TitanCashierDesk wxPublicCashierDesk = this.buildCashierDeskList(
                    cashierDeskInitRequest, PaySourceEnum.TT_MALL_MOBILE).get(0);
            titanCashierDeskDao.saveCashierDesk(wxPublicCashierDesk);
        }
        //添加机构费率
        List<String> rateUserIdlist = titanRateConfigDao.queryUserIdNoRateConfig(BusTypeEnum.WX_PUBLIC.type);
        if (CollectionUtils.isNotEmpty(rateUserIdlist)) {
            List<TitanRateConfig> rateConfiglist = new ArrayList<TitanRateConfig>();
            int i = 0, size = 0;
            for (String userid : rateUserIdlist) {
                i++;
                size++;
                rateConfiglist.add(buildPayRateConfig(BusTypeEnum.WX_PUBLIC, userid, "微信公众号支付费率"));
                if (i == 10 || size == rateUserIdlist.size()) {//每10个或者最后一个提交。
                    titanRateConfigDao.batchSaveRateConfigs(rateConfiglist);
                    i = 0;
                    rateConfiglist.clear();
                }
            }
        }


    }*/

    @Override
    public <T> void addModelOfPayment(PaymentItemRequest<T> request) {
        if (null == request || !StringUtil.isValidString(request.getType()) || request.getItem() == null) {
            log.error("增加支付选项参数错误");
            return;
        }

        TitanCashierDeskItem item = new TitanCashierDeskItem();
        item.setItemtype(Integer.parseInt(request.getType()));
        List<TitanCashierDeskItem> items = titanCashierDeskItemDao.queryCashierDeskItems(item);
        List<TitanCashierItemBank> banks = new ArrayList<TitanCashierItemBank>();
        if (request.getItem() instanceof ItemBankDTO) {
            ItemBankDTO bankDTO = (ItemBankDTO) request.getItem();
            for (TitanCashierDeskItem itm : items) {
                TitanCashierItemBank bank = new TitanCashierItemBank();
                MyBeanUtil.copyProperties(bank, bankDTO);
                bank.setItemid(itm.getItemid());
                banks.add(bank);


            }
        }
        titanCashierItemBankDao.batchSaveItemBanks(banks);
    }
    
    @Override
    public BaseResponseDTO initTradingPlatformCashierdesk() {
    	 
    	 BaseResponseDTO baseResponseDTO = new BaseResponseDTO();
    	 baseResponseDTO.putSysError();
    	 List<TitanCashierDeskItem> cashierDeskItems = new ArrayList<TitanCashierDeskItem>();
    	 
    	 int delCount = titanCashierDeskItemDao.delCashierdeskItemForTradingPlatform();
    	 log.info("delCashierdeskItemForTradingPlatform is success, delCount:" + delCount);
    	 
    	 List<CashierDeskDTO> cashierDeskList = titanCashierDeskItemDao.queryTradingPlatformCashierdesk();
    	 if(CollectionUtils.isEmpty(cashierDeskList)){
    		 baseResponseDTO.putSuccess("no cashierDesk need to init");
    		 return baseResponseDTO;
    	 }
    	 
    	 for (CashierDeskDTO desk : cashierDeskList) {
    		 cashierDeskItems.add(buildCashierDeskItem(desk.getDeskId(), CashierItemTypeEnum.B2B_ITEM));
             cashierDeskItems.add(buildCashierDeskItem(desk.getDeskId(), CashierItemTypeEnum.B2C_ITEM));
             cashierDeskItems.add(buildCashierDeskItem(desk.getDeskId(), CashierItemTypeEnum.CREDIT_ITEM));
             cashierDeskItems.add(buildCashierDeskItem(desk.getDeskId(), CashierItemTypeEnum.QR_ITEM));
             cashierDeskItems.add(buildCashierDeskItem(desk.getDeskId(), CashierItemTypeEnum.BALANCE_ITEM));
    	 }
    	 //批量插入初始化收银台子项
    	 int insertDeskItemCount = 0;
         for (TitanCashierDeskItem deskItem : cashierDeskItems) {
        	 insertDeskItemCount++;
             titanCashierDeskItemDao.saveCashierDeskItem(deskItem);
         }
    	 log.info("batchSaveCashierDeskItem success, insertDeskItemCount:" + insertDeskItemCount);
         
         //默认初始化银行：
    	 int count = 0;
         List<TitanCashierItemBank> allItemBanks = new ArrayList<TitanCashierItemBank>();
         for (TitanCashierDeskItem deskItem : cashierDeskItems) {
             if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.B2B_ITEM.getItemCode())) {
                 allItemBanks.addAll(buildItemBankList(deskItem.getItemid(), "B2B"));
             }
             if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.B2C_ITEM.getItemCode())) {
                 allItemBanks.addAll(buildItemBankList(deskItem.getItemid(), "B2C"));
             }
             if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.CREDIT_ITEM.getItemCode())) {
                 allItemBanks.addAll(buildItemBankList(deskItem.getItemid(), "Credit"));
             }
             if (deskItem.getItemtype() == Integer.valueOf(CashierItemTypeEnum.QR_ITEM.getItemCode())) {
                 allItemBanks.addAll(buildQRBankList(deskItem.getItemid()));
             }
             if(allItemBanks.size() >= 10000){
            	 count++;
            	 int insertItemBanksCount = titanCashierItemBankDao.batchSaveItemBanks(allItemBanks);
            	 log.info("batchSaveItemBanks_" + count + " success, insertItemBanksCount:" + insertItemBanksCount);
            	 allItemBanks = new ArrayList<TitanCashierItemBank>();
             }
         }
         if(CollectionUtils.isNotEmpty(allItemBanks)){
        	 int insertItemBanksCount = titanCashierItemBankDao.batchSaveItemBanks(allItemBanks);
        	 log.info("batchSaveItemBanks_" + (count+1) + " success, insertItemBanksCount:" + insertItemBanksCount);
         }
         log.info("initTradingPlatformCashierdesk success");
         
         baseResponseDTO.putSuccess("initTradingPlatformCashierdesk success");
    	 return baseResponseDTO;
    	
    }

}
