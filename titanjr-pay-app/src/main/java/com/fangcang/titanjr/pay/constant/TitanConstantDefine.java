package com.fangcang.titanjr.pay.constant;

/**
 * 收银台常量信息定义
 * 
 * @author wengxitao
 *
 */
public final class TitanConstantDefine {

	// 收银台默认错误地址
	public static final String TRADE_PAY_ERROR_PAGE = "/checkstand-pay/cashierDeskError";

	// 充值页面
	public static final String RECHARGE_MAIN_PAGE = "/account-overview/account-recharge";

	// 收银台页面
	public static final String CASHIER_DESK_MAIN_PAGE = "/checkstand-pay/cashierDesk";
	
	// 新版收银台
	public static final String CASHIER_DESK_MAIN_PAGE_NEW = "/checkstand-upgrade/cashierDeskNew";
	
	// 退款页面
	public static final String REFUND_MAIN_PAGE = "/account-overview/refund";

	// 外部付款人中间账户
	public static final String EXTERNAL_PAYMENT_ACCOUNT = "141223100000056";

	// 默认用户私钥
	public static final String PRIVATE_KEY = "30820276020100300d06092a864886f70d0101010500048202603082025c02010002818100d8b6e03dd8f9bf45157f0d14aedf9a696665641da90cab5114a22b7f6c711f22429c32c99ab76e3ce74de00145bcd50b9d2e7c60cd97a4979a5d0ce4ead9ba61baca1495758d69cc1f76e69db43f1ef1f9c33cd2edb8c726ed17c297a7b9fa3f18e58aef9d3f33f8431a41cc3c0ca7bc5151d33a8691e6506e0439363aec0063020301000102818027846d6e89b6bce48d8f6de4b420ad0904357fe492b36f37e941cb19c0bdfdf5e2dc95bc427ca95aecb8bc1caf4948360672f81634d72e99c079b044bbf878ee4c3c6050d319fc545736e392fa7dcf2761d23551663d3844f3f2f61f652bec45eedf6d398a7e0916944bba8c64dd70f770ba4e213764e97c2aeaf46e66a3b591024100f7e35b74120740e22c85c2ddae516bcde62648447f2269b3701503afe4775749d7ffba66eff7024b2b6e879b6b9f3945508eb189d7fe602488575dae609bf151024100dfce5d97595fce458c1769fea32b175bb594a404ea070009c1c139d63907acad0433cc55ffcbf7c81359057efbda4f968813ed34dfedd9edb9079fdfd486c97302406832ad9290b173d89e966b5efb9346197a90c4f7e5e8f53d73f3a1652247f7ed165a6c6430a247d8891d20eb77c5aa3134b7867146d5aa5c30e3688190227cc1024100ad298b8a75d145d4d3aeae0922104e335c0c14d7e486e405a88f2b83cf7e5ba14676196c94cd28faf9d550064f313b9119da691717077e2d8b9315a4e6581f770240177a20e4fc96cb896a899ef9a5bc4c54c24b416e2fda7debf7536e851fb33fdeb95750742b0f09154acd53d73af8750461bca5bd7cc0de58a8fa635453152e78";
	
	// 收银台 md5签名 key
	public static final String PAY_APP_CASHIER_SIGN_MD5_KEY = "Yz5:@xts6uP2&!vP";
	
	//通联支付标示
	public static final String TLPAY_CHANNEL_CODE = "01";
	
	//融宝支付标示
	public static final String RBPAY_CHANNEL_CODE = "02";
	
	//通联提现基准费率记录的支付方式用0表示
	public static final String TL_WITHDRAW_PAYTYPE = "0";
	
}
