package com.sq.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.sq.gmall.annotations.LoginRequired;
import com.sq.gmall.bean.OmsOrder;
import com.sq.gmall.bean.PaymentInfo;
import com.sq.gmall.payment.config.AlipayConfig;
import com.sq.gmall.service.order.OrderService;
import com.sq.gmall.service.payment.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @title: PaymentController
 * @Description
 * @Author sq
 * @Date: 2020/8/19 20:43
 * @Version 1.0
 */
@Controller
public class PaymentController {
    @Reference
    private OrderService orderService;
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private PaymentService paymentService;
    /**
     * 微信支付
     * @param orderId
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(String orderId,HttpServletRequest request,ModelMap modelMap){
        return null;
    }

    /**
     * 支付宝支付成功更新支付状态
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String aliPayCallBackReturn(HttpServletRequest request,ModelMap modelMap){

        //回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String tradeNo = request.getParameter("trade_no");
        String outTradeNo = request.getParameter("out_trade_no");
        String callBackContent = request.getQueryString();

        //通过支付宝的paramsMap进行签名验证,2.0版本接口将paramsMap参数去掉了,导致同步请求无法验签
        if(StringUtils.isNotBlank(sign)&&StringUtils.isNotBlank(tradeNo)&&StringUtils.isNotBlank(callBackContent)){
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(outTradeNo);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(tradeNo);//支付宝交易凭证
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(callBackContent);//回调请求字符串
            //更新支付信息(支付成功,发送消息到消息队列(订单服务更新---库存服务---物流))
            paymentService.updatePayment(paymentInfo);
        }

        return "finish";
    }

    /**
     * 支付宝支付
     * @param orderId
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String orderId,HttpServletRequest request,ModelMap modelMap){
        //创建api对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        //根据外部订单号获取订单
        OmsOrder omsOrder = orderService.findByOrderSn(orderId);
        BigDecimal payAmount = omsOrder.getPayAmount();
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",orderId);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",payAmount);
        map.put("subject","谷粒商城"+orderId);
        String param = JSON.toJSONString(map);
        //获取支付宝请求客户端
        String form = null;
        try {
            //调用sdk生成表单
            form = alipayClient.pageExecute(alipayRequest).getBody();

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //生成并保存用户的支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("谷粒商城"+orderId);
        paymentInfo.setTotalAmount(payAmount);
        paymentInfo.setOrderSn(orderId);
        paymentService.savePaymentInfo(paymentInfo);

        //发送消息到延迟消息队列(向支付宝查找支付状态)
        paymentService.sendDelayPaymentResultCheckQueue(orderId,5);

        //提交请求到支付宝

        return form;
    }

    /**
     * 跳转支付选择页面
     * @param orderId
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String orderId, HttpServletRequest request, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //根据用户id和订单号查询订单数据
       OmsOrder omsOrder = orderService.findByMemberId(memberId,orderId);
        //订单外部号
        String orderSn = omsOrder.getOrderSn();
        //支付价格
        BigDecimal payAmount = omsOrder.getPayAmount();
        modelMap.put("nickName",nickname);
        modelMap.put("orderId",orderSn);
        modelMap.put("totalAmount",payAmount);
        return "index";
    }
}
