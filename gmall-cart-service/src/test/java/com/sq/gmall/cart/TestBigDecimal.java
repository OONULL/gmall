package com.sq.gmall.cart;

import java.math.BigDecimal;

/**
 * @title: TestBigDecimal
 * @Description
 * @Author sq
 * @Date: 2020/8/10 18:21
 * @Version 1.0
 */
public class TestBigDecimal {

    public static void main(String[] args) {
        //初始化
        BigDecimal f = new BigDecimal(0.01f);
        BigDecimal d = new BigDecimal(0.01d);
        BigDecimal s = new BigDecimal("0.01");
        System.out.println(f);
        System.out.println(d);
        System.out.println(s);

        //比较
        int i = f.compareTo(d);//1 0  -1
        System.out.println(i);
        //运算
        f.add(d);//加
        BigDecimal subtract = f.subtract(d);//减
        f.multiply(d);//乘
        f.divide(d,3,BigDecimal.ROUND_HALF_DOWN);//除(保留3位小数,并四舍五入)

        //约数
        BigDecimal scale = subtract.setScale(3, BigDecimal.ROUND_HALF_DOWN);//(保留3位小数,并四舍五入)
        System.out.println(scale);
    }
}
