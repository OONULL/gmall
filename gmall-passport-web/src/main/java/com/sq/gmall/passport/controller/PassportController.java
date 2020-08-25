package com.sq.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.sq.gmall.bean.UmsMember;
import com.sq.gmall.service.user.UserService;
import com.sq.gmall.util.HttpclientUtil;
import com.sq.gmall.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @title: PassportController
 * @Description
 * @Author sq
 * @Date: 2020/8/11 15:23
 * @Version 1.0
 */
@Controller
public class PassportController {
    @Reference
    private UserService userService;
    @Value("${weibo.tokenUrl}")
    private String tokenUrl;
    @Value("${weibo.clientId}")
    private String clientId;
    @Value("${weibo.clientSecret}")
    private String clientSecret;
    @Value("${weibo.grantType}")
    private String grantType;
    @Value("${weibo.redirectUri}")
    private String redirectUri;
    @Value("${weibo.userShowUrl}")
    private String userShowUrl;
    /**
     * 通过jwt校验token真假
     * @param code
     * @return
     */
    @RequestMapping("vlogin")
    public String vlogin(String code, HttpServletRequest request){

        Map<String,String> map = new HashMap<>();
        map.put("client_id",clientId);
        map.put("client_secret",clientSecret);
        map.put("grant_type",grantType);
        map.put("redirect_uri",redirectUri);
        map.put("code",code);

        String accessTokenJson = HttpclientUtil.doPost(tokenUrl+"?", map);
        if(org.apache.commons.lang3.StringUtils.isBlank(accessTokenJson)){
           throw new RuntimeException("微博登录错误,请重试!");
        }
        Map<String,String> accessTokenMap = JSON.parseObject(accessTokenJson, Map.class);
        //access_token换取用户信息
        String uid = accessTokenMap.get("uid");
        String accessToken = accessTokenMap.get("access_token");
        String userUrl = userShowUrl+"?access_token="+accessToken+"&uid="+uid;
        String userJson = HttpclientUtil.doGet(userUrl);
        Map<String,Object> userMap = JSON.parseObject(userJson, Map.class);
        //将用户信息保存数据库,用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType(2);
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(accessToken);
        umsMember.setSourceUid(String.valueOf(userMap.get("idstr")));
        umsMember.setCity((String) userMap.get("location"));
        String gender = (String) userMap.get("gender");
        int genderint = 0;
        if("m".equals(gender)){
            genderint = 1;
        }else if("f".equals(gender)){
            genderint = 2;
        }
        umsMember.setGender(genderint);
        umsMember.setNickname((String) userMap.get("screen_name"));
        //判断微博用户是否已经保存到数据库
        UmsMember check = new UmsMember();
        check.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userService.checkOauthUser(check);
        if(StringUtils.isEmpty(umsMemberCheck)){
            umsMember = userService.addUser(umsMember);
        }else {
            umsMember = umsMemberCheck;
        }
        //生成jwt的token,并重定向到首页,携带该token
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        String token ="fail";
        token = makeToken(memberId,nickname,request);
        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }

    /**
     * 通过jwt校验token真假
     * @param token
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp){
        //通过jwt校验token真假
        Map<String,String> map = new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token,"2020gmall0801",  currentIp);

        if(!CollectionUtils.isEmpty(decode)){
            map.put("status","success");
            map.put("memberId",(String) decode.get("memberId"));
            map.put("nickname",(String) decode.get("nickname"));
        }

        return JSON.toJSONString(map);
    }

    /**
     * 登录操作
     * @param umsMember
     * @param request
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        //调用用户服务验证用户名与密码
       UmsMember umsMemberLogin = userService.login(umsMember);
        String token = null;
       if(!StringUtils.isEmpty(umsMemberLogin)){
           //登录成功(jwt制作token)
           String memberId = umsMemberLogin.getId();
           String nickname = umsMemberLogin.getNickname();
           token = makeToken(memberId,nickname,request);
       }else {
           //登录失败
           token = "fail";
       }
        return token;
    }

    /**
     * 跳转登录页面(登录完成跳转回之前页面)
     * @param returnUrl
     * @param modelMap
     * @return
     */
    @RequestMapping("index")
    public String index(String returnUrl, ModelMap modelMap){

        modelMap.put("returnUrl",returnUrl);
        return "index";
    }

    /**
     * 生成token
     * @param memberId
     * @param nickname
     * @param request
     * @return
     */
    private String makeToken(String memberId,String nickname ,HttpServletRequest request){
        String token = "fail";
        Map<String,Object>map = new HashMap<>();
        map.put("memberId",memberId);
        map.put("nickname",nickname);

        //通过nginx转发的客户端ip
        String ip = request.getHeader("x-forwarded-for");
        if(org.apache.commons.lang3.StringUtils.isBlank(ip)){
            ip= request.getRemoteAddr();
            if(org.apache.commons.lang3.StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }

        token = JwtUtil.encode("2020gmall0801",map,ip);
        //将token存入redis
        userService.addUserToken(token,memberId);

        return token;
    }
}
