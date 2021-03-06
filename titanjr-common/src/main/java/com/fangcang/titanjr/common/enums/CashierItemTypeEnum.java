package com.fangcang.titanjr.common.enums;

import com.fangcang.util.StringUtil;

/**
 * Created by zhaoshan on 2016/5/18.
 */
public enum CashierItemTypeEnum {

    B2B_ITEM("1", "企业网银"), B2C_ITEM("2", "个人网银"), CREDIT_ITEM("3", "信用卡"),
    BALANCE_ITEM("4", "账户余额"), FINANCING_ITEM("5", "理财"), MOBILE_ITEM("6", "移动端"), WX_PUBLIC("8", "微信公众号支付"),
    QR_ITEM("9", "第三方支付"), LOAN("10", "贷款支付"), QUICK_PAY_NEW("11", "新快捷支付");

    public String itemCode;
    public String itemName;

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public static CashierItemTypeEnum getCashierItemTypeEnumByKey(String key) {
        if (!StringUtil.isValidString(key)) {
            return null;
        }

        for (CashierItemTypeEnum cashierItemTypeEnum : CashierItemTypeEnum
                .values()) {
            if (cashierItemTypeEnum.itemCode.equals(key)) {
                return cashierItemTypeEnum;
            }
        }
        return null;
    }

    /**
     * 判断是否个人网银
     *
     * @author Jerry
     * @date 2017年7月25日 上午11:21:48
     */
    public static boolean isPersonalEbank(String itemCode) {
        if (itemCode == null) {
            return false;
        }

        if (itemCode.equals(B2C_ITEM.itemCode)) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否信用卡支付
     *
     * @author Jerry
     * @date 2017年7月25日 上午11:23:48
     */
    public static boolean isCreditCard(String itemCode) {
        if (itemCode == null) {
            return false;
        }

        if (itemCode.equals(CREDIT_ITEM.itemCode)) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否快捷支付
     *
     * @author Jerry
     * @date 2017年7月25日 上午11:38:23
     */
    public static boolean isQuickPay(String itemCode) {
        if (itemCode == null) {
            return false;
        }

        if (itemCode.equals(QUICK_PAY_NEW.itemCode)) {
            return true;
        }

        return false;
    }
    
    /**
     * 快捷支付和第三方支付需要返回json数据
     * @author Jerry
     */
    public static boolean needReturnJson(String itemCode) {
        if (itemCode == null) {
            return false;
        }

        if (itemCode.equals(QUICK_PAY_NEW.itemCode) || itemCode.equals(QR_ITEM.itemCode)) {
            return true;
        }

        return false;
    }
    
    /**
     * <p>余额支付和第三方支付不需要保存为常用支付方式</p>
     * <p>新版收银台，快捷支付需要在支付请求（发送验证码）成功后增加到常用支付方式，以确保信息正确</p>
     * @author Jerry
     * @date 2017年9月20日 上午11:23:17
     */
    public static boolean isNeedSaveCommonpay(String itemCode) {
        if (itemCode == null) {
            return false;
        }

        if (!itemCode.equals(BALANCE_ITEM.itemCode) && !itemCode.equals(QR_ITEM.itemCode) && !itemCode.equals(QUICK_PAY_NEW.itemCode)) {
            return true;
        }

        return false;
    }

    private CashierItemTypeEnum(String itemCode, String itemName) {
        this.itemCode = itemCode;
        this.itemName = itemName;
    }
}
