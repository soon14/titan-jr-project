<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
 <%@ include file="/comm/taglib.jsp"%>
		
		<div style="margin-right:17px;margin-left: 17px; " class="label">
				<table width="100%" cellspacing="0" border="0">
				<colgroup>					
					<col width="50">					
					<col width="11%">
					<col width="9%">
					
					<col width="9%">
					
					<col width="9%">
					
					<col width="9%">
					<col width="5%">
					<col width="8%">
					<col width="14%">
					<col width="8%">			
					<col width="">
				</colgroup>
				<tbody>
				<tr>					
					<td class="tdl">序号</td>
					<td width="tdl">贷款申请时间</td>
					<td class="tdr">放款贷款额度(元)</td>
					
					<td class="tdr">已还本金(元)</td>
					
					<td class="tdr">已付利息(元)</td>
					
					<td class="tdr">剩余本金(元)</td>	
					<td></td>
					<td class="tdl">贷款类型</td>
					<td class="tdl">还款方式</td>
					<td class="tdl">贷款状态</td>
					<td class="tdl">操作</td>
				</tr>
			</tbody></table>
		</div>
		<div class="MyAssets_listContent">
			<table cellpadding="0" cellspacing="0" width="100%" class="MyAssets_listTable">
				<colgroup>					
					<col width="50">					
					<col width="11%">
					<col width="9%">
					
					<col width="9%">
					
					<col width="9%">
					
					<col width="9%">
					<col width="5%">
					<col width="8%">
					<col width="14%">
					<col width="8%">			
					<col width="">
				</colgroup>
				
				<c:forEach items="${loanInfoList}" var="loanInfoItem" varStatus="status">
				<tr>				
					<td width="">${status.index +1}</td>
					<td width=""><fmt:formatDate value="${loanInfoItem.createTime}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
					<td class="tdr"><fmt:formatNumber value="${loanInfoItem.actualAmount  /100}"  pattern="#,##0.00#" /></td>
					
					<td class="tdr"><fmt:formatNumber value="${loanInfoItem.repaymentPrincipal /100}"  pattern="#,##0.00#" /></td>
					
					<td class="tdr"><fmt:formatNumber value="${loanInfoItem.repaymentInterest /100}"  pattern="#,##0.00#" /></td>
					
					<td class="tdr"><fmt:formatNumber value="${loanInfoItem.shouldCapital /100}"  pattern="#,##0.00#" /></td>
					<td></td>
					<td width="" >
						<c:if test="${loanInfoItem.productType==10001}">
							包房贷
						</c:if>
						<c:if test="${loanInfoItem.productType==10002}">
							运营贷
						</c:if>
					</td>
					<td width="" >
						<span style="max-width: 142px">
							<c:if test="${loanInfoItem.repaymentType==1}">
								按日计利，随借随还
							</c:if>
						</span>
					</td>
					<td>
						<c:if test="${loanInfoItem.status==0}">
						无效贷款
					</c:if>
					<c:if test="${loanInfoItem.status==1}">
						审核中
					</c:if>
					
					<c:if test="${loanInfoItem.status==2}">
						待终审
					</c:if>
					
					<c:if test="${loanInfoItem.status==3}">
						终审通过
					</c:if>
					
					<c:if test="${loanInfoItem.status==4}">
						 终审失败
					</c:if>
					
					<c:if test="${loanInfoItem.status==5}">
						待放款
					</c:if>
					
					<c:if test="${loanInfoItem.status==6}">
						待还款
					</c:if>
					<c:if test="${loanInfoItem.status==7}">
						放款失败
					</c:if>
					<c:if test="${loanInfoItem.status==8}">
						贷款失败
					</c:if>
					<c:if test="${loanInfoItem.status==9}">
						<span class="MyAssets_red">已逾期</span>
					</c:if>
					<c:if test="${loanInfoItem.status==10}">
						已结清
					</c:if>
					<c:if test="${loanInfoItem.status==11}">
							已撤銷
					</c:if>
					</td>				
					<td class=""><a class="blue decorationUnderline m_r10 " href="<%=basePath%>/loan/getLoanDetailsInfo.shtml?orderNo=${loanInfoItem.orderNo}">详情</a> 
						<c:if test="${loanInfoItem.status==1 || loanInfoItem.status==2 || loanInfoItem.status==3 || loanInfoItem.status==5}">
<%-- 							 <a class="blue decorationUnderline J_revocation" orderNo="${loanInfoItem.orderNo}" href="javascript:void(0)">撤销申请</a> --%>
						</c:if>
						<c:if test="${loanInfoItem.status==6 || loanInfoItem.status==9}">
							<a class="blue decorationUnderline" href="<%=basePath%>/loan/repayment/repaymentPer.shtml?orderNo=${loanInfoItem.orderNo}">还款</a>
						</c:if>
						
					</td>
				</tr>
				</c:forEach>
			</table>
		</div>
<div id="kkpager" class="page_turning"></div>
<script type="text/javascript">

	var pageSize =' ${pageSize}';
	var totalRecords = '${totalCount}';
	var totalPage =parseInt( (totalRecords % pageSize) == 0 ? totalRecords / pageSize
			: (totalRecords / pageSize) + 1);

	var pageNo = 1;
	//生成分页
	//有些参数是可选的，比如lang，若不传有默认值
	var pageObj = new Pager({
		pno : pageNo,
		//总页码
		total : totalPage,
		//总数据条数
		totalRecords : totalRecords,
		isShowTotalRecords : true,
		isGoPage : true,
		mode : 'click',//默认值是link，可选link或者click

		click : function(n) {
			pageClick(n);
			//手动选中按钮
			this.selectPage(n);
			return false;
		}
	});
	
	
	//撤销申请
	$('.J_revocation') .on( 'click',
		function() {
		
			var orderNo = $(this).attr("orderNo");
			
			window.top.createConfirm({
						title : '提示',
						content : '<div style="font-size:15px;line-height:30px;">确定要撤销贷款申请吗？</div>',
						okValue : '确定',
						cancelValue : '取消',
						ok : function() {
						top.F.loading.show();
						$.ajax({
							type : 'get',
							url :  '<%=basePath%>/loan/stopLoan.shtml'+"?DateTime="+new Date().getTime() +"&orderNo=" +orderNo,
							dataType : 'json',
							success : function(result) {
									if(result.code==1){
										setTimeout(function() {
											new window.top.Tip({
												msg : '贷款申请已撤销'
											});
										}, 1000);
										
									}else{
										new top.Tip({msg : result.msg, type: 3, timer:2500});
									}
									
									top.F.loading.hide();
								}
							});
							 
						},
						cancel : function() {

						}
			});
		});
	
</script>