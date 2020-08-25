package com.sq.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.sq.gmall.annotations.LoginRequired;
import com.sq.gmall.util.CookieUtil;
import com.sq.gmall.util.HttpclientUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 拦截器
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //拦截代码

        //判断被拦截的请求的访问方式的注解(是否需要拦截)
        HandlerMethod hm = (HandlerMethod)handler;
        //获取方法上的注解
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        //判断是否有该注解
        if(StringUtils.isEmpty(methodAnnotation)){
            //不存在则放行
            return true;
        }

        String token = "";
        //判断之前是否登录过
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        //判断现在是否登录
        String newToken = request.getParameter("token");
        if(org.apache.commons.lang3.StringUtils.isNotBlank(oldToken)){
            token = oldToken;
        }
        if(org.apache.commons.lang3.StringUtils.isNotBlank(newToken)){
            token = newToken;
        }
        //获取是否对用户登录判定
        boolean loginSuccess = methodAnnotation.loginSuccess();

        //调用认证中心进行验证
        String success = "fail";
        Map<String,String> successMap = new HashMap<>();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(token)){
            //通过nginx转发的客户端ip
            String ip = request.getHeader("x-forwarded-for");
            if(org.apache.commons.lang3.StringUtils.isBlank(ip)){
                ip= request.getRemoteAddr();
                if(org.apache.commons.lang3.StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
          String  successJson = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token+"&currentIp="+ip);

            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");

        }


        if(loginSuccess){
            //登录成功才可使用
            if(!"success".equals(success)){
                //重定向到passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?returnUrl="+requestURL);
                return false;
            }
            //将token携带的用户信息写入
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));
            //验证通过,覆盖cookie中的token
            if(org.apache.commons.lang3.StringUtils.isNotBlank(token)){
                CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
            }

        }else {
            //不登陆也可使用,必须验证
            if("success".equals(success)){
                //将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
                //验证通过,覆盖cookie中的token
                if(org.apache.commons.lang3.StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                }
            }
        }


        return true;
    }
}