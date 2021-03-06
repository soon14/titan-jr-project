<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ include file="/comm/taglib.jsp"%>
<!DOCTYPE html>
<html>
<meta charset="utf-8">
    <title>设置登录密码-泰坦钱包</title>
   <jsp:include page="/comm/tws-static-resource.jsp"></jsp:include>
	<jsp:include page="/comm/static-js.jsp"></jsp:include>
	<script type="text/javascript" src="<%=cssSaasPath%>/js/jquery.mCustomScrollbar.concat.min.js"></script>
	
</head>
<body style="min-width: 1300px;" class="bg" >
<jsp:include page="/comm/head-title.jsp">
	<jsp:param value="设置登录密码" name="title"/>
</jsp:include>

<div class="register r_three forget">
	<div class="r_box login_pwd_set">		
		<div class="r_c ">
			<div class="r_tit">请输入登录密码并确认：</div>
			<div class="r_text">
				<ul>					
					<li class="r_y2"><input type="password" class="text" name="newLoginPassword" id="newLoginPassword" placeholder="设置登录密码" datatype="/\w*/" afterPassed="checkPass" errormsg="必填项"><i class="ico rt_eye"></i><em class="ico hint_1" id="newLoginPassword_hint" style="display:none;"></em></li>
					<li class="r_y3"><input type="password" class="text" name="passwordConfirm" id="passwordConfirm" placeholder="确认登录密码" datatype="/\w*/" afterPassed="confirmPass" errormsg="必填项"><i class="ico rt_eye "></i></li>				
					<li class="lb_btn"><a href="javascript:;" class="" onclick="savePass()">确定</a></li>
				</ul>
			</div>			
		</div>		
	</div>
</div>

<!-- 版权 -->
<jsp:include page="/comm/foot.jsp"></jsp:include>
<script type="text/javascript">
//验证
var pwdForm =  new validform('.login_pwd_set');
//显示密码
$('.rt_eye').on('click',function(){
	var _this=$(this);
	if(_this.is('.rt_open')){
		_this.parent().find('input').attr('type','password');
		_this.removeClass('rt_open');
	}else{		
		_this.parent().find('input').attr('type','text');
		_this.addClass('rt_open');
	}
});
function checkPass(value, inputDom){
	if(value.length==0){
		pwdForm.setErrormsg(inputDom,'必填项');
		return false;
	}
	if(value.length<6){
		pwdForm.setErrormsg(inputDom,'密码太简单');
		return false;
	}
	return true;
}
//确认密码框失去焦点
function confirmPass(){
	var pass1 = $("#newLoginPassword").val();
	var pass2 = $("#passwordConfirm").val();
	if($.trim(pass2).length==0){
		pwdForm.setErrormsg($("#passwordConfirm"),"必填项");
		return false;
	}
	if(pass1!=pass2){
		pwdForm.setErrormsg($("#passwordConfirm"),"两次输入密码不一致");
		return false;
	}
	return true;
}
function savePass(){
	if(!pwdForm.validate()){
		return ;
	}
	var newLoginPassword = $("#newLoginPassword").val();
	$.ajax({
		type:"post",
		url: '<%=basePath%>/setting/set-login-password.shtml',
		data:{"newLoginPassword":newLoginPassword},
		dataType:"json",
		success:function(result){
			if(result.code==1){
				location.href="<%=basePath%>/ex/pwd-set-succ.shtml?pageType=1";
			}else{
				new top.Tip({msg : result.msg, type: 2, timer:2000});
			}
		}
	});
}
$('#newLoginPassword').bind("keyup",function(){
	if($(this).val().length==0){
		$("#newLoginPassword_hint").hide();
		return;
	}
	var m = checkComplicacy($(this).val());
	if(m>=3){
		m = 3;
	}
	$("#"+$(this).attr("id")+'_hint').attr({'class':"ico hint_"+m});
	$("#newLoginPassword_hint").show();
});

</script>

</body>
</html>