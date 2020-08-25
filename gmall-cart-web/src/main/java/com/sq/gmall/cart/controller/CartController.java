package com.sq.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.sq.gmall.annotations.LoginRequired;
import com.sq.gmall.bean.OmsCartItem;
import com.sq.gmall.bean.PmsSkuInfo;
import com.sq.gmall.bean.PmsSkuSaleAttrValue;
import com.sq.gmall.service.cart.CartService;
import com.sq.gmall.service.manage.SkuService;
import com.sq.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @title: CartController
 * @Description
 * @Author sq
 * @Date: 2020/8/8 21:39
 * @Version 1.0
 */
@Controller
public class CartController {

    @Reference
    private SkuService skuService;
    @Reference
    private CartService cartService;

    /**
     * 更新购物车
     * @param isChecked
     * @param skuId
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @LoginRequired(loginSuccess = false)
    @RequestMapping("checkCart")
    public String checkCart(String isChecked,String skuId,HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){
        //用户
        String userId = (String) request.getAttribute("memberId");
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(userId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        //修改商品状态
        cartService.checkCart(omsCartItem);

        //查询返回购物车列表
        List<OmsCartItem> omsCartItemList = cartService.cartList(userId);
        modelMap.put("cartList", omsCartItemList);
        //结算总价
        BigDecimal totalAmount = getTotalAmount(omsCartItemList);
        modelMap.put("totalAmount", totalAmount);
        return "cartListInner";
    }

    /**
     * 修改购物车内商品数量
     * @param quantity
     * @param skuId
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("reduceQuantity")
    @LoginRequired(loginSuccess = false)
    public String reduceQuantity(String quantity,String skuId,HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){
        //用户
        String userId = (String) request.getAttribute("memberId");
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(userId);
        if(Double.parseDouble(quantity)<1){
            quantity = "1";
        }
        if(Double.parseDouble(quantity)>200){
            quantity = "200";
        }
        BigDecimal bigDecimal = new BigDecimal(quantity);
        omsCartItem.setQuantity(bigDecimal);
        omsCartItem.setProductSkuId(skuId);
        //修改商品状态
        cartService.checkCart(omsCartItem);

        //查询返回购物车列表
        List<OmsCartItem> omsCartItemList = cartService.cartList(userId);
        modelMap.put("cartList", omsCartItemList);
        //结算总价
        BigDecimal totalAmount = getTotalAmount(omsCartItemList);
        modelMap.put("totalAmount", totalAmount);
        return "cartListInner";
    }



    /**
     * 获取购物车列表信息
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        //判断用户是否登录
        String userId = (String) request.getAttribute("memberId");
        if(StringUtils.isNotBlank(userId)){
            //已登录(缓存中查询购物车)
            omsCartItemList = cartService.cartList(userId);
        }else {
            //未登录(cookie中查询购物车)
            String cartListCookieJson = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookieJson)){
                omsCartItemList = JSON.parseArray(cartListCookieJson,OmsCartItem.class);
            }

        }
        modelMap.put("cartList", omsCartItemList);
        //结算总价
       BigDecimal totalAmount = getTotalAmount(omsCartItemList);
        modelMap.put("totalAmount", totalAmount);
        return "cartList";
    }

    /**
     * 获取结算的总价
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

    /**
     * 添加商品到购物车
     * @param skuId
     * @param quantity 数量
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, long quantity, HttpServletRequest request, HttpServletResponse response) {

        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

        //将商品信息封装购物车
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111111111");
        omsCartItem.setProductSkuId(skuId);
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            if (i == 0) {
                omsCartItem.setSp1(skuSaleAttrValueList.get(i).getSaleAttrValueName());
            }
            if (i == 1) {
                omsCartItem.setSp2(skuSaleAttrValueList.get(i).getSaleAttrValueName());
            }
            if (i == 2) {
                omsCartItem.setSp3(skuSaleAttrValueList.get(i).getSaleAttrValueName());
            }
        }

        omsCartItem.setQuantity(new BigDecimal(quantity));

        List<OmsCartItem> cartItemList = new ArrayList<>();
        String addSkuId = omsCartItem.getProductSkuId();
        //判断用户是否登录

        String memberId = (String) request.getAttribute("memberId");
        if (StringUtils.isBlank(memberId)) {
            //未登录将商品存入cookie
            //获取cookie中的购物车商品信息json字符串
            String cartListCookieJson = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //cookie不为空
            if (StringUtils.isNotBlank(cartListCookieJson)) {
                //转化为购物车商品
                cartItemList = JSON.parseArray(cartListCookieJson, OmsCartItem.class);
                //判断添加的购物车数据是否在购物车存在
                for (OmsCartItem cartItem : cartItemList) {
                    String productSkuId = cartItem.getProductSkuId();
                    if (addSkuId.equals(productSkuId)) {
                        cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                    } else {
                        cartItemList.add(omsCartItem);
                    }
                }
            } else {
                //cookie为空
                //创建购物车集合
                cartItemList = new ArrayList<>();
                cartItemList.add(omsCartItem);
            }
            // 更新cookie
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(cartItemList), 60 * 60 * 72, true);
        } else {
            //以登录将商品加入用户购物车
            //查询数据库中用户购物车数据
            OmsCartItem cartItem = cartService.getCartByUserAndSkuId(memberId, skuId);
            if (!org.springframework.util.StringUtils.isEmpty(cartItem)) {
                String productSkuId = cartItem.getProductSkuId();
                if (addSkuId.equals(productSkuId)) {
                    //数据库存在商品更新
                    cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                    cartService.update(cartItem);
                }else {
                    //数据库不存在该用户购物车(添加)
                    omsCartItem.setMemberId(memberId);
                    cartService.add(omsCartItem);
                }
            } else {
                //数据库不存在该用户购物车(添加)
                omsCartItem.setMemberId(memberId);
                cartService.add(omsCartItem);
            }
            //同步缓存
            cartService.flushCartCache(memberId);
        }


        return "redirect:/success.html";
    }
}
