package com.fangcang.titanjr.common.enums;

import com.fangcang.util.StringUtil;

public enum PayerTypeEnum {

    B2B_PUS("1", "B2B交易平台"),
    B2B_GDP("2", "GDP"),
    SUPPLY_FINACIAL("3", "财务供应商"),
    SUPPLY_UNION("4", "联盟供应商付款"),
    ALLIANCE("5", "商家联盟"),
    RECHARGE("7", "充值"),
    WITHDRAW("8", "提现"),
    OPEN_ORG("1001", "对外开放平台"),
    TT_MALL("1024", "TTMALL收银台"),
    TT_MALL_V2("10242","TTMALL收银台v2"),
    B2B_WX_PUBLIC_PAY("10243" , "微信公众号支付"),
    LOAN("512", "贷款");

    public String key;

    public String getKey() {
        return key;
    }

    public String getMsg() {
        return msg;
    }

    public String msg;

    private PayerTypeEnum(String key, String msg) {
        this.key = key;
        this.msg = msg;
    }

    public static PayerTypeEnum getPayerTypeEnumByKey(String key) {
        if (!StringUtil.isValidString(key)) {
            return null;
        }

        for (PayerTypeEnum payerTypeEnum : PayerTypeEnum.values()) {
            if (payerTypeEnum.key.equals(key)) {
                return payerTypeEnum;
            }
        }
        return null;
    }


    public static Integer getPaySource(String key) {
        if (RECHARGE.key.equals(key)) {//充值收银台
            return 5;
        } else if (B2B_PUS.key.equals(key) || B2B_GDP.key.equals(key)) {
            return 1;
        } else if (SUPPLY_FINACIAL.key.equals(key) || SUPPLY_UNION.key.equals(key)) {
            return 2;
        } else if (ALLIANCE.key.equals(key)) {
            return 3;
        } else if (OPEN_ORG.key.equals(key)) {
            return 6;
        } else if (TT_MALL.key.equals(key)||TT_MALL_V2.key.equals(key)) {
            return 7;
        } else if( B2B_WX_PUBLIC_PAY.key.equals(key))
        {
        	return 8;
        }
        return null;
    }

    public boolean isRechargeCashDesk() {
        return RECHARGE.key.equals(this.key);
    }

    //使用收款方收银台
    public boolean useReceiverCashDesk() {
        return B2B_PUS.key.equals(this.key) || B2B_WX_PUBLIC_PAY.key.equals(this.key)|| B2B_GDP.key.equals(this.key) || OPEN_ORG.key.equals(this.key) || TT_MALL.key.equals(this.key)|| TT_MALL_V2.key.equals(this.key);
    }

    public boolean isUserId() {
        return RECHARGE.key.equals(this.key) || WITHDRAW.key.equals(this.key) || LOAN.key.equals(this.key);
    }

    //接收方为机构编码的
    public boolean receiverIsMerchantCode() {
        return B2B_PUS.key.equals(this.key) || B2B_GDP.key.equals(this.key) || SUPPLY_UNION.key.equals(this.key) || TT_MALL.key.equals(this.key);
    }

    public boolean isFcUserId() {
        return SUPPLY_FINACIAL.key.equals(this.key) || SUPPLY_UNION.key.equals(this.key);
    }

    public boolean isPayerNecessary() {
        return SUPPLY_FINACIAL.key.equals(this.key) || SUPPLY_UNION.key.equals(this.key);
    }

    public boolean isPayeeNecessary() {
        return SUPPLY_UNION.key.equals(this.key) || B2B_PUS.key.equals(this.key) || B2B_GDP.key.equals(this.key)
                || RECHARGE.key.equals(this.key) || OPEN_ORG.key.equals(this.key) || TT_MALL.key.equals(this.key)|| TT_MALL_V2.key.equals(this.key);
    }


    public boolean isB2BPayment() {
        return B2B_PUS.key.equals(this.key) || B2B_GDP.key.equals(this.key) || B2B_WX_PUBLIC_PAY.key.equals(this.key);
    }

    public boolean isAddAccountHistory() {
        return SUPPLY_FINACIAL.key.equals(this.key);
    }

    public boolean isOpenOrg() {
        return OPEN_ORG.key.equals(this.key);
    }

    public boolean isTTMAlL() {
        return TT_MALL.key.equals(this.key);
    }

    public boolean isWithdraw() {
        return WITHDRAW.key.equals(this.key);
    }

    public boolean isSupplyFinancial() {
        return SUPPLY_FINACIAL.key.equals(this.key);
    }

    public boolean isSupplyUnion() {
        return SUPPLY_UNION.key.equals(this.key);
    }

    public boolean isTtMall() {
        return TT_MALL.key.equals(this.key);
    }
    
    public boolean isTTmallV2(){
    	 return TT_MALL_V2.key.equals(this.key);
    }

    public boolean isLoan() {
        return LOAN.key.equals(this.key);
    }
}
