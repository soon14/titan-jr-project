package com.titanjr.fop.constants;

/**
 * Created by zhaoshan on 2017/12/21.
 */
public enum ReturnCodeEnum {
    CODE_USERID_ERROR("-100","userid不合法"),
    CODE_CONVERT_ERROR("-101","参数转换失败"),
    SERVICE_URL_ERROR("-102","获取支付实际请求URL错误"),
    CODE_SIGN_ERROR("-200","sign不合法"),
    CODE_SESSION_ERROR("-201","session信息不合法"),
    CODE_OPERTYPE_ERROR("-202","opertype信息不合法"),
    CODE_SYS_ERROR("-300","系统错误");
    private String code;
    private String msg;

    ReturnCodeEnum(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}