<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
 <%@ include file="/comm/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<title>SAAS后台管理</title>
	<jsp:include page="/comm/static-resource.jsp"></jsp:include>
	<jsp:include page="/comm/tfs-static-resource.jsp"></jsp:include>
<body>
<div id="scroll">  
    <div class="main_top clearfix headline bg_bom">
         <div class="main_return fl"><a href="javascript:history.go(-1)"><i></i>返回上一页</a></div>
        <div class="history fl">我的贷款 > 包房贷款详情</div>
    </div>   
</div>

<div class="Refund clearfix">
	

	
	<div class="R_title"><i></i>贷款详情</div>
	
	
	<div class="R_c clearfix">
	<c:if test="${loanOrderInfo.status==1}">
		<div class="RC_img RC_img03"></div>
	</c:if>
	<c:if test="${loanOrderInfo.status==2}">
		<div class="RC_img RC_img03"></div>
	</c:if>
	<c:if test="${loanOrderInfo.status==4}">
		<div class="RC_img RC_img01"></div>
	</c:if>
	<c:if test="${loanOrderInfo.status==5}">
		<div class="RC_img RC_img01"></div>
	</c:if>
	<c:if test="${loanOrderInfo.status==8}">
		<div class="RC_img RC_img02"></div>
	</c:if>
	<c:if test="${loanOrderInfo.status==7}">
		<div class="RC_img"></div>
	</c:if>	
		<div class="RC_t">
			<ul>
				<li>贷款类型：包房贷</li>
				<li>贷款编号：${loanOrderInfo.orderNo}</li>
				<li>申请时间：<fmt:formatDate value="${loanOrderInfo.createTime}" pattern="yyyy-MM-dd" /></li>
				<li>放款时间：<fmt:formatDate value="${loanOrderInfo.relMoneyTime}" pattern="yyyy-MM-dd" /></li>
			</ul>
		</div>
		<div class="RC_c clearfix">
			<ul>
				<li class="clearfix"><div class="tit">贷款金额</div><i class="c_666 f_20"><fmt:formatNumber value="${loanOrderInfo.actualAmount/100}"  pattern="#,##0.00#" /></i> <i class="c_666 ">元</i> <i class="c_999 m_l50">贷款最长期限为90天，日费率0.5‰，到期日前随借随还</i></li>
				<li class="clearfix"><div class="tit">收款信息</div>
					<div class="w_290" title="账户名：烈扬旅游">账户名：${loanSpecInfo.accountName}</div>
					<div class="w_360">银行账号：${loanSpecInfo.account}</div>
					<div class="w_400">开户行：${loanSpecInfo.bank}</div>
				</li>
				<li class="clearfix"><div class="tit">贷款内容</div>
					<div class="w_300">酒店名称：${loanSpecInfo.hotleName}</div>
					<div class="w_360">包房时段：<fmt:formatDate value="${loanSpecInfo.beginDate}" pattern="yyyy-MM-dd" /> -- <fmt:formatDate value="${loanSpecInfo.endDate}" pattern="yyyy-MM-dd" /></div>
					<div class="w_400">包房数量：${loanSpecInfo.roomNights}间夜</div>
				</li>
				<li class="clearfix p_l83"><div class="tit">附件信息</div>
				<dl id="imgList">
					
				</dl>				
				</li>
			</ul>
		</div>
		<c:if test="${loanOrderInfo.status==3 || loanOrderInfo.status==6}">
			<div class="RC_t1 p_t10">
				<span class="w_105">还款记录</span>
				<span class="w_240">还款到期日：<fmt:formatDate value="${loanOrderInfo.actualRepaymentDate}" pattern="yyyy-MM-dd" /></span>
				<span class="w_240">剩余待还本息：<fmt:formatNumber value="${loanOrderInfo.shouldCapital/100}"  pattern="#,##0.00#" /> 元</span>		
				<a class="btn" href="泰坦金融-我的贷款首页-还款1.html">马上还款</a>
			</div>
		</c:if>
		<c:if test="${loanOrderInfo.status==6 || loanOrderInfo.status==7 || loanOrderInfo.status==3}">
		<div class="RC_c1 RC_c2 clearfix">
			<table cellpadding="0" cellspacing="0" width="900">
				<colgroup>
					<col width="85">
					<col width="">
					<col width="">
					<col width="">
					<col width="">
					<col width="">
				</colgroup>
				<tr>
					<td><i>序号</i></td>
					<td><i>还款日期</i></td>
					<td class="tdr"><i>还款金额（元）</i></td>
					<td class="tdr"><i>还本金（元）</i></td>
					<td class="tdr"><i>付利息（元）</i></td>
					<td class="tdr"><i>剩余本金（元）</i></td>
				</tr>
				<tr>
					<td>1</td>
					<td>2016-09-10</td>
					<td class="tdr">8,345.00</td>
					<td class="tdr">8,345.00</td>
					<td class="tdr">345.00</td>
					<td class="tdr">8,345.00</td>
				</tr>
				<tr>
					<td>2</td>
					<td>2016-09-10</td>
					<td class="tdr">8,345.00</td>
					<td class="tdr">8,345.00</td>
					<td class="tdr">345.00</td>
					<td class="tdr">8,345.00</td>
				</tr>
				<tr>
					<td>3</td>
					<td>2016-09-10</td>
					<td class="tdr">8,345.00</td>
					<td class="tdr">8,345.00</td>
					<td class="tdr">345.00</td>
					<td class="tdr">8,345.00</td>
				</tr>
				<tr>
					<td>4</td>
					<td>2016-09-10</td>
					<td class="tdr">8,345.00</td>
					<td class="tdr">8,345.00</td>
					<td class="tdr">345.00</td>
					<td class="tdr">8,345.00</td>
				</tr>
			</table>
		</div>
		</c:if>
		
		<c:if test="${loanOrderInfo.status==6}">
			<div class="RC_t1 ">
				<span class="w_105 c_fe2b2b">未通过原因</span>
				<span class="c_fe2b2b">贷款金额过大</span>
			</div>
		</c:if>
	</div>

</div>	
	
<div style="height: 60px"></div>
<div class="TFS_data_button">

	<c:if test="${loanOrderInfo.status==8}">
		<a class="btn btnNext" href="<%=basePath %>/loan_apply/main.shtml">重新申请</a>
	</c:if>
	
	<c:if test="${loanOrderInfo.status==1 || loanOrderInfo.status==2}">
	
		<a class="btn btnNext" href="泰坦金融-我的贷款首页-申请包房贷款.html">撤销申请</a>
		
	</c:if>
	
    <a class=" btn_exit_long bnt_exit_padding12" href="javascript:history.go(-1)">返回</a>
</div>

<jsp:include page="/comm/static-js.jsp"></jsp:include>
<script type="text/javascript">  
var staticPath  ="http://image.fangcang.com/upload/images/titanjr/loan_apply/${JR_USERID}/${loanOrderInfo.orderNo}/";

 $(function(){
     
	 	var imgList = "${loanSpecInfo.contractUrl}";
	 	
	 	if(imgList != null && imgList != '')
	 	{
	 		var imgsrcs = imgList.split(",");
	 		for(var i=0;i<imgsrcs.length;i++)
	 		{
	 			if(imgsrcs[i]!= null)
	 			{
	 				var src = staticPath+ imgsrcs[i];
	 				$('#imgList').append('<dd class="TFSimgOnBig"><div class="dd_img"><img src="'+src+'">'
	 				+'<div class="hover">下载</div></div><div class="dd_text" title="附件附件">附件附件.jpg</div></dd>');
	 			}
	 		}
	 	}
	 	
	 
        function bigImgShow(){              
           var _index=0; 
           var leftBtn,rightBtn,ulDiv;
            $(".TFSimgOnBig").find("img").live('click',function(){
            var _div='<div tabindex="0" style=" position: fixed; left: 0px; top: 0px; width: 100%; height: 100%; overflow: hidden; -webkit-user-select: none; z-index: 1024; background-color: rgba(0, 0, 0,0.7);" class="bigImgShow_box_bg"><span class="left_btn"></span><span class="right_btn"></span><div class="bigImgShow_box"><span class="close_btn"></span><ul>';     
            _index=$(this).parents(".TFSimgOnBig").index();
            console.log()
            for(var i=0;i< $(".TFSimgOnBig").find("img").length;i++){
                var img=$(".TFSimgOnBig").eq(i).find("img").attr("src");
                 if(i==_index){
                   _div+="<li class='cur MissionRoom_annexList_li'><img src='"+img+"'></li>" 
                }else{
                   _div+="<li class='MissionRoom_annexList_li'><img src='"+img+"'></li>"  
                }                              
            };
            _div+="</ul></div></div>";
            window.top.$("body").append(_div);
            leftBtn=window.parent.$(".left_btn");
            rightBtn=window.parent.$(".right_btn");
            var closeBtn=window.parent.$(".close_btn");
            var divId=window.parent.$(".bigImgShow_box_bg");
            ulDiv=window.parent.$(".bigImgShow_box").find("ul");
            if(_index==0){
                leftBtn.hide();
            };
            if(_index==$(".TFSimgOnBig").length-1){
                rightBtn.hide();
            };        
            leftBtn.on('click',function(){
               _index--;
               if(_index==0){
                 leftBtn.hide();
               };
               rightBtn.show();
               showImg();
           }); 
           rightBtn.on('click',function(){                 
               _index++;
               if(_index==$(".TFSimgOnBig").length-1){
                 rightBtn.hide();
               };
               leftBtn.show();
               showImg();
           });  
           closeBtn.live('click',function(){                 
              divId.remove();
           });          
        });
           
           function showImg(){
            ulDiv.find(".MissionRoom_annexList_li").hide().eq(_index).show(); 
           } 
         };   
         new bigImgShow(); 
       $(".TFSaddImg").live('click',function(){
            $(this).addClass("hidden");
            $(this).parent().find(".TFSuploading").removeClass("hidden");
            loading($(this).parent().find(".TFSuploading"));
       });
       $(".J_delete_upload").live('click',function(){
            $(this).parent().addClass("hidden").removeClass("TFSimgOnBig");
            $(this).parent().parent().find(".TFSaddImg").removeClass("hidden");
       });
       function loading(obj){
           var l1=obj.find("span");
           var l2=obj.find("i");
           var i=0;
           var loadingJ=setInterval(function(){
                l1.css("width",i+"%");
                l2.html(i);
                if(i==90){
                    clearInterval(loadingJ);
                }
                i++;
        　　},20); 
           setTimeout(function(){
             var loadingN=setInterval(function(){
                l1.css("width",i+"%");
                l2.html(i);
                if(i==100){
                    clearInterval(loadingN);
                }
                i++;
        　　},20);
                             
           },2500);
           setTimeout(function(){
             obj.addClass("hidden");  
             obj.parent().find(".TFSimgOn").removeClass("hidden").addClass("TFSimgOnBig");
           },3000);
       }
    })
	

</script>
</body>
</html>