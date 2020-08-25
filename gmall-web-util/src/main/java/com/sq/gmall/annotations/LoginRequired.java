package com.sq.gmall.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @title: LoginRequired
 * @Description
 * @Author sq
 * @Date: 2020/8/11 17:31
 * @Version 1.0
 */
@Target(ElementType.METHOD)//只在方法上使用
@Retention(RetentionPolicy.RUNTIME)//运行时有效
public @interface LoginRequired {

    boolean loginSuccess() default true;
}
