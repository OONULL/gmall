package com.sq.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sq.gmall.annotations.LoginRequired;
import com.sq.gmall.bean.OmsCartItem;
import com.sq.gmall.bean.OmsOrder;
import com.sq.gmall.bean.OmsOrderItem;
import com.sq.gmall.bean.UmsMemberReceiveAddress;
import com.sq.gmall.service.cart.CartService;
import com.sq.gmall.service.manage.SkuService;
import com.sq.gmall.service.order.OrderService;
import com.sq.gmall.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @title: OrderController
 * @Description 结算
 * @Author sq
 * @Date: 2020/8/15 20:21
 * @Version 1.0
 */
@Controller
public class OrderController {

    @Reference
    private CartService cartService;
    @Reference
    private UserService userService;
    @Reference
    private OrderService orderService;
    @Reference
    private SkuService skuService;

    /**
     * 提交订单
     * @param receiveAddressId 结算地址id
     * @param totalAmount 总金额
     * @param tradeCode 交易码
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @LoginRequired(loginSuccess = true)
    @RequestMapping("submitOrder")
    public ModelAndView submitOrder(String receiveAddressId, String totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        ModelAndView modelAndView = new ModelAndView();

        //检查校验码
       String success = orderService.checkTradeCode(memberId,tradeCode);
       if("success".equals(success)){
           List<OmsOrderItem> omsOrderItems = new ArrayList<>();
           OmsOrder omsOrder = new OmsOrder();
           //将毫秒时间戳加字符串时间拼接成外部订单号
           String orderSn ="gmall"+System.currentTimeMillis()+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
           //自动确认收货时间
            omsOrder.setAutoConfirmDay(7);
            //订单创建时间
            omsOrder.setCreateTime(new Date());
            //订单状态(待付款)
           omsOrder.setStatus("0");
           //折扣
           omsOrder.setDiscountAmount(null);
           //运费
           //omsOrder.setFreightAmount();
           omsOrder.setMemberId(memberId);
           omsOrder.setMemberUsername(nickname);
           omsOrder.setNote("备注");
           omsOrder.setOrderSn(orderSn);//外部订单号
           omsOrder.setOrderType(1);//订单类型
           BigDecimal payAmount = new BigDecimal("0");

           //获取收货人信息
           UmsMemberReceiveAddress umsMemberReceiveAddress =userService.getReceiveAddressById(receiveAddressId);
           omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
           omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
           omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
           omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
           omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
           omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
           omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
           omsOrder.setSourceType(0);//订单来源
           //当前日期加一天
           Calendar calendar = Calendar.getInstance();
           calendar.add(Calendar.DATE,1);
           omsOrder.setReceiveTime(calendar.getTime());//配送日期
           //校验成功
           //根据用户id获取要购买的商品列表
           List<OmsCartItem> omsCartItemList = cartService.cartList(memberId);
           for (OmsCartItem omsCartItem : omsCartItemList) {
               if("1".equals(omsCartItem.getIsChecked())){
                    //订单详情
                   OmsOrderItem omsOrderItem = new OmsOrderItem();
                   //验证价格
                   if(!skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice())){
                       modelAndView.setViewName("tradeFail");
                       return modelAndView;
                   }
                   //验证库存

                   payAmount = payAmount.add(omsCartItem.getTotalPrice());
                   omsOrderItem.setProductPic(omsCartItem.getProductPic());
                   omsOrderItem.setProductName(omsCartItem.getProductName());
                   omsOrderItem.setOrderSn(orderSn);//订单号
                   omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                   omsOrderItem.setProductPrice(omsCartItem.getPrice());
                   omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                   omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                   omsOrderItem.setProductSkuCode("11111111");
                   omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                   omsOrderItem.setProductId(omsCartItem.getProductId());
                   omsCartItem.setProductSn("仓库中商品编号");//仓库中的skuid
                   omsOrderItem.setSp1(omsCartItem.getSp1());
                   omsOrderItem.setSp2(omsCartItem.getSp2());
                   omsOrderItem.setSp3(omsCartItem.getSp3());
                   omsOrderItems.add(omsOrderItem);

               }
           }
           omsOrder.setPayAmount(payAmount);//订单支付价格
           omsOrder.setTotalAmount(payAmount);
           omsOrder.setOmsOrderItems(omsOrderItems);
           //将订单和订单详情写入数据库
           //购物车删除对应商品
           String orderId = orderService.saveOrder(omsOrder);
           //重定向到支付页面
           modelAndView.setViewName("redirect:http://payment.gmall.com:8087/index");
           modelAndView.addObject("orderId",orderId);
           return modelAndView;
       }

        modelAndView.setViewName("tradeFail");
        return modelAndView;
    }
    /**
     * 跳转结算页面
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @LoginRequired(loginSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //购物车集合
        List<OmsCartItem> omsCartItemList = cartService.cartList(memberId);
        //用户地址列表
        List<UmsMemberReceiveAddress> receiveAddressList = userService.getReceiveAddressByMemberId(memberId);

        List<OmsOrderItem> omsOrderItemList = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItemList) {
            if("1".equals(omsCartItem.getIsChecked())){
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItemList.add(omsOrderItem);
            }
        }
        String tradeCode =orderService.generateTradeCode(memberId);
        modelMap.put("omsOrderItemList",omsOrderItemList);
        modelMap.put("userAddressList",receiveAddressList);
        modelMap.put("nickName",nickname);
        modelMap.put("totalAmount",getTotalAmount(omsCartItemList));
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }

    /**
     * 获取结算商品总金额
     * @param omsCartItemList
     * @return
     */
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItemList) {
        BigDecimal bigDecimal = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItemList) {
            if("1".equals(omsCartItem.getIsChecked())){
                bigDecimal = bigDecimal.add(omsCartItem.getTotalPrice());
            }
        }
        return bigDecimal;
    }
}
