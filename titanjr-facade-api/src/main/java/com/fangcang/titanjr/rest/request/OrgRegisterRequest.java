package com.fangcang.titanjr.rest.request;

import com.fangcang.titanjr.request.BaseRequest;
import com.fangcang.titanjr.rest.enums.RegisterSourceEnum;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Created by zhaoshan on 2017/8/1.
 */
@ApiModel(value = "OrgRegisterRequest", description = "机构注册请求")
public class OrgRegisterRequest extends BaseRequest {

    //机构相关数据
    @ApiModelProperty(value = "合作方机构编码",required = true)
    private String coopOrgCode;
    @ApiModelProperty(value = "合作方机构名称",required = true)
    private String coopOrgName;
    @ApiModelProperty(value = "联系人")
    private String connect;
    @ApiModelProperty(value = "联系电话")
    private String connectTel;
    @ApiModelProperty(value = "地址")
    private String address;
    @ApiModelProperty(value = "电子邮件")
    private String email;
    @ApiModelProperty(value = "注册来源" , allowableValues = "SAAS_MERCHANT, TTM_SUPPLY, TTM_AGENT", required = true)
    private RegisterSourceEnum registerSourceEnum;

    //初始用户相关数据
    @ApiModelProperty(value = "合作方用户ID，若同时添加用户则必须")
    private String coopUserId;
    @ApiModelProperty(value = "合作方用户名")
    private String coopUserName;
    @ApiModelProperty(value = "合作方用户登录名，若同时添加用户则必须")
    private String userLoginName;
    @ApiModelProperty(value = "是否管理员，若同时添加用户则必须")
    private boolean isAdmin;

    public String getCoopOrgCode() {
        return coopOrgCode;
    }

    public void setCoopOrgCode(String coopOrgCode) {
        this.coopOrgCode = coopOrgCode;
    }

    public String getCoopOrgName() {
        return coopOrgName;
    }

    public void setCoopOrgName(String coopOrgName) {
        this.coopOrgName = coopOrgName;
    }

    public String getConnect() {
        return connect;
    }

    public void setConnect(String connect) {
        this.connect = connect;
    }

    public String getConnectTel() {
        return connectTel;
    }

    public void setConnectTel(String connectTel) {
        this.connectTel = connectTel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RegisterSourceEnum getRegisterSourceEnum() {
        return registerSourceEnum;
    }

    public void setRegisterSourceEnum(RegisterSourceEnum registerSourceEnum) {
        this.registerSourceEnum = registerSourceEnum;
    }

    public String getCoopUserId() {
        return coopUserId;
    }

    public void setCoopUserId(String coopUserId) {
        this.coopUserId = coopUserId;
    }

    public String getCoopUserName() {
        return coopUserName;
    }

    public void setCoopUserName(String coopUserName) {
        this.coopUserName = coopUserName;
    }

    public String getUserLoginName() {
        return userLoginName;
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName = userLoginName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
