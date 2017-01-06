<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ include file="/comm/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
    <title>注册申请-泰坦钱包</title>
    <jsp:include page="/comm/static-resource.jsp"></jsp:include>
	<jsp:include page="/comm/tfs-static-resource.jsp"></jsp:include>
	<jsp:include page="/comm/static-js.jsp"></jsp:include>
</head>
  
  <body>
   
   
   
<script type="text/javascript">
var email_reg=/^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
var phone_reg=/^13[0-9]{9}$|^14[0-9]{9}$|^15[0-9]{9}$|^18[0-9]{9}$|^17[0-9]{9}$/;
F.UI.scan();
//验证
var valid_qy_form = new validform('#reg_form_qy');
var valid_phone_form = new validform('#reg_form_phone');
var valid_email_form = new validform('#reg_form_email');
 

var current_form = "reg_form_qy";
//企业还是个人tab
var TAB_TYPE={"enterprise":1,"personage":2};
//选择是手机号码还是邮箱注册
var LOGIN_TYPE={"email":1,"phone":2};
var current_tab = TAB_TYPE["enterprise"];
var loginType = LOGIN_TYPE["email"];




//企业用户 个人用户 
$('.enterprise').click(function(){
	//$('.next').unbind("click",next);
    var qy = $(this).parents('.pour').find('.qy'),
        gr = $(this).parents('.pour').find('.gr');

        qy.show();    
        gr.hide();
        $(this).addClass('on').siblings().removeClass('on');
		
        $('.f_ui-checkbox-c3 input[type=checkbox]').attr('checked',false);
       // $('.btn').addClass('btn_h');
        current_tab = TAB_TYPE["enterprise"];
        loginType = LOGIN_TYPE["email"];
         
});    
$('.personage').click(function(){
	//	$('.next').unbind("click",next);
        var qy = $(this).parents('.pour').find('.qy'),
        gr = $(this).parents('.pour').find('.gr');
        qy.hide();
        gr.show();
        $(this).addClass('on').siblings().removeClass('on');

        $('.f_ui-checkbox-c3 input[type=checkbox]').attr('checked',false);
       // $('.btn').addClass('btn_h');
        current_tab = TAB_TYPE["personage"];
        loginType = LOGIN_TYPE[$('.personage').attr("data-select-form")];
});




$('.J_yx').click(function() {
	//$('.next').unbind("click",next);
    if($('.yx1').is('.on')){
        $('.yx1').hide().removeClass('on');
        $('.yx2').show().addClass('on');
        $(this).text('使用手机号码注册>>');
        $('.f_ui-checkbox-c3 input[type=checkbox]').attr('checked',false);
        //$('.btn').addClass('btn_h');
        loginType = LOGIN_TYPE["email"];
        $('.personage').attr({"data-select-form":"email"});
    }else
    { 
        $('.yx1').show().addClass('on');
        $('.yx2').hide().removeClass('on');      
        $(this).text('使用邮箱注册>>');
        $('.f_ui-checkbox-c3 input[type=checkbox]').attr('checked',false);
        //$('.btn').addClass('btn_h');
        loginType = LOGIN_TYPE["phone"];
        $('.personage').attr({"data-select-form":"phone"});
    }
});
//显示密码
$('.eye').on('click',function(){  
    var _this= $(this);
    var a= $(this).parent().find('input');    
    if (_this.is('.on')) {
        a[0].type = "password"; 
        $(this).removeClass('on')
    }else{
        a[0].type = "text"; 
        $(this).addClass('on');        
    }  
});
  
//判断当前显示的formid
function getCurrentFormId(){
	if(current_tab==TAB_TYPE["enterprise"]){
		current_form = "reg_form_qy";
		return current_form;
	}
	if(loginType==LOGIN_TYPE["phone"]){
		current_form = "reg_form_phone";
		return current_form;
	}
	if(loginType==LOGIN_TYPE["email"]){
		current_form = "reg_form_email";
		return current_form;
	}
	return "reg_form_qy";
}
//表单id后缀
function getformSuffix(){
	if(current_tab==TAB_TYPE["enterprise"]){
		return "qy";
	}
	if(loginType==LOGIN_TYPE["phone"]){
		return "phone";
	}
	if(loginType==LOGIN_TYPE["email"]){
		return "email";
	}
	return "qy";
	
}
//表单组件
function getValidate(){
	var suffix = getformSuffix();
	if(suffix=="qy"){
		return valid_qy_form;
	}
	if(suffix=="phone"){
		return valid_phone_form;
	}
	if(suffix=="email"){
		return valid_email_form;
	}
}
//确认密码框失去焦点
function confirmPass(){
	var formId = getCurrentFormId();
	var pass1 = $("#"+formId+" .pass1").val();
	var pass2 = $("#"+formId+" .pass2").val();
	if(pass1!=pass2){
		getValidate().setErrormsg($("#"+formId+" .pass2"),"两次输入密码不一致");
		return false;
	}
	return true;
}

//下一步
function next(){
	if(!getValidate().validate()){
		return;
	}
	
	
	//我已阅读并同意
	if(!$('.f_ui-checkbox-c3 input[type=checkbox]').is(':checked'))
	{
		   new top.createConfirm({
		        padding:'30px 20px 65px',
		        width:'330px',
		        title:'提示',
		        content : '<div class="f_14 t_a_c">请阅读并勾选《泰坦云金融服务协议》！</div>',      
		        button:false
		      }); 
		   return;
	}
	
	var formId = getCurrentFormId();
	var loginEle = $("#"+formId+" .ui-loginusername");
	var regCodeEle = $("#"+formId+" .ui-reg");
	var userLoginName = loginEle.val();
	var regCode = regCodeEle.val();
	
	$.ajax({
		type:"post",
		url: '<%=basePath%>/organ/checkRegCode.shtml',		
		data:{"userLoginName":userLoginName,"regCode":regCode},
		dataType:"json",
		success:function(result){
			if(result.code==1){
				var formId = getCurrentFormId();
				 $("#"+formId+" .regbtn").click();
			}else{
				new top.createConfirm({
			        padding:'30px 20px 65px',
			        width:'330px',
			        title:'提示',
			        content : '<div class="f_14 t_a_c">'+result.msg+'</div>',      
			        button:false
			     }); 
			}
			
		},
		error:function(){
			new top.Tip({msg : '请求错误，请重试', type: 3, timer:2000});
		}
	});
}

//泰坦云金融服务协议
$('.J_agreement').on('click',function(){
        $.ajax({
            dataType : 'html',
            context: document.body,
            url : '<%=basePath%>/organ/showAgreement.shtml',
            success : function(html){
                var d =  window.top.dialog({
                    title: ' ',
                    padding: '0 0 20px 0',
                    content: html,
                    skin : 'saas_pop',                  
                    button : [ 
                        {
                            value: '我已阅读并同意此协议',
                            skin : 'btn p_lr30',
                            callback: function () {                               
                            },
                            autofocus: true
                        }
                    ],
                    close : function(){                        
                    }
                }).showModal();
            }
        })
    });


//验证码错误
$('.J_mistake').on('click',function(){
        new top.createConfirm({
        padding:'30px 20px 65px',
        width:'330px',
        title:'提示',
        content : '<div class="f_14 t_a_c">验证码错误！</div>',      
        button:false
      }); 
});

//获取验证码
function timeOut(_this){
    var i=60;
    var interval=setInterval(function () {                
         if(i>0){
             _this.html("重新发送(" + i + ")"); 
             i--;
         }else{
            _this.removeClass("huise").html("获取验证码");
            clearInterval(interval);
         }; 
    }, 1000);
};
//发送验证码
$('.verify').on('click',function(){
	var formId = getCurrentFormId();
	var loginEle = $("#"+formId+" .ui-loginusername");
	var receiveAddress = loginEle.val();
    //
    if($.trim(receiveAddress).length==0){
    	new top.Tip({msg : '用户名不能为空', type: 1, timer:2000});
    	loginEle.focus();
    	return
    }
	if(formId=='reg_form_1_qy'||formId=='reg_form_3_email'){
		if(!email_reg.test(receiveAddress)){
			getValidate().setErrormsg(loginEle,'邮箱格式不正确');
			loginEle.focus();
			return ;
		}
	}else if(formId=='reg_form_2_phone'){
		if(!phone_reg.test(receiveAddress)){
			getValidate().setErrormsg(loginEle,'手机号码格式不正确');
			loginEle.focus();
			return ;
		}
    }
	_this = $(this);
	$.ajax({
		async : false,
		data:{"receiveAddress":receiveAddress,"msgType":1},
		url : '<%=basePath%>/organ/sendCode.shtml',
		dataType : 'json',
		success : function(result){
			if(result.code==1){
				if(!_this.hasClass("huise")){
			        new top.Tip({msg : '验证码已成功发送,请注意查收！', type: 1, timer:2000});    
			        _this.addClass('huise');
			        timeOut(_this);
			    } 
			}else{
				new top.Tip({msg : result.msg, type: 3, timer:2500});
			}
		},
		error : function(){
			new top.Tip({msg : '请求错误，请重试', type: 3, timer:2000});
		}
	});
});
 


$('.next').bind("click",next);

//检查是否已经注册
function checkExist(value, inputDom){
	var flag = false;
	$.ajax({
		async:false,
		type:'post',
		data:{"userLoginName":value,"isOperator":0},
		url : '<%=basePath%>/organ/checkUserLoginName.shtml',
		dataType : 'json',
		success : function(result){
			if(result.code==1){
				flag = true;
			}else{
				flag = false;
				//修改错误提示信息
				getValidate().setErrormsg(inputDom,result.msg);
			}
		}
	});
	return  flag;
}

$('.pass1').bind("cut copy paste", function(e) {  
    e.preventDefault();  
});  
$('.pass2').bind("cut copy paste", function(e) {  
    e.preventDefault();  
});  

//还原提示语
$('.ui-loginusername').on('change',function(){
	var formId = getCurrentFormId();
	var suffix = getformSuffix();
	var loginEle = $("#"+formId+" .ui-loginusername");
	if(suffix=="qy"||suffix=="email"){
		getValidate().setErrormsg(loginEle,'邮箱格式不正确');
		return;
	}
	if(suffix=="phone"){
		getValidate().setErrormsg(loginEle,'手机号码格式不正确');
		return;
	}
});


if('${param.userType}' == "2")
{
	$('.personage').click();
	
}

if('${ param.regUserType}'=="2")
{
	$('.J_yx').click();
}
if('${param.userType}' == "2")
{
$('.f_ui-checkbox-c3 input[type=checkbox]').attr('checked',true);
}

</script>
  </body>
</html>
