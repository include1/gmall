package com.zm.gmall.passport.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.zm.gmall.bean.UmsMember;

import com.zm.gmall.service.UmsMemberService;
import com.zm.gmall.util.CookieUtil;
import com.zm.gmall.util.HttpClientUtil;
import com.zm.gmall.util.JwtUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.swing.StringUIClientPropertyKey;

import javax.print.attribute.HashAttributeSet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


@Controller
public class PassPortController {

    @Reference
    UmsMemberService umsMemberService;

    //第三方退出
    @RequestMapping("vlogout")
    public String vlogout(String code, HttpServletRequest request, HttpServletResponse reponse){
        CookieUtil.deleteCookie(request,reponse,"oldToken");
        return "redirect:http://localhost:8084/index";
    }
    //第三方百度登录
    @RequestMapping("baidu/login")
    public String baiduLogin(String code,HttpServletRequest request){
       //通过code授权码，获取token
        String s = "https://openapi.baidu.com/oauth/2.0/token?";
        Map<String,String> paramMap = new HashMap<>();
//                grant_type=authorization_code&
//                code=CODE&
//                client_id=YOUR_CLIENT_ID&
//                client_secret=YOUR_CLIENT_SECRET&
//                redirect_uri=YOUR_REGISTERED_REDIRECT_URI
        paramMap.put("grant_type","authorization_code");
        paramMap.put("code",code);
        paramMap.put("client_id","SItTz5ANkVxMglRG7g9iLI1M");
        paramMap.put("client_secret","LORRyEh1nI1PGmHX4yfaiVACiaPcFqvB");
        paramMap.put("redirect_uri","http://localhost:8086/baidu/login");
        String tokenJson = HttpClientUtil.doPost(s, paramMap);
        String access_token = null;
        if(!StringUtils.isBlank(tokenJson)){
            Map<String,String> map = JSON.parseObject(tokenJson, Map.class);
            access_token = map.get("access_token");
        }
        //通过获得的access_token，获取用户信息
        String userJson = HttpClientUtil.doGet("https://openapi.baidu.com/rest/2.0/passport/users/getLoggedInUser?access_token="+access_token);
        UmsMember umsMember = new UmsMember();
        if(!StringUtils.isBlank(userJson)){
            Map<String,String> map = JSON.parseObject(userJson, Map.class);
            umsMember.setSourceUid(map.get("uid"));
            UmsMember oauthUser = umsMemberService.getOauthUser(umsMember);
            umsMember.setNickname(map.get("uname"));
            umsMember.setSourceType("4");
            umsMember.setAccessToken(access_token);
            umsMember.setAccessCode(code);
            if(oauthUser == null){
                //添加到数据库
                umsMemberService.addOauthUser(umsMember);
                oauthUser = umsMemberService.getOauthUser(umsMember);
            }
            umsMember = oauthUser;
        }
        //生成token
        String token = "";
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",umsMember.getId());
        userMap.put("nickname",umsMember.getNickname());
        String key = "20191111gmall";
        token = getToken(key,request,userMap);

        return "redirect:http://localhost:8084/index?token="+token;
    }

    //第三方新浪登录
    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request){

        //通过code(授权码)，获取token
        String s3 = "https://api.weibo.com/oauth2/access_token?";//client_id=3917917273&client_secret=17fa10b8e615b31c311300a5336bd6a3&grant_type=authorization_code&redirect_uri=http://127.0.0.1:8086/vlogin&code=7bf86dd73e3f935ebc64f001da73f4fe";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","3917917273");
        paramMap.put("client_secret","17fa10b8e615b31c311300a5336bd6a3");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://127.0.0.1:8086/vlogin");
        paramMap.put("code",code);
        String access_token_json = HttpClientUtil.doPost(s3,paramMap);
        //获取用户信息
        String accessToken = null;
        String uId = null;
        if(!StringUtils.isBlank(access_token_json)){
            Map<String,String> map = JSON.parseObject(access_token_json, Map.class);
            accessToken = map.get("access_token");
            uId = map.get("uid");
        }
        String userJsonStr = HttpClientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token="+accessToken+"&uid="+uId);

        Map<String,Object> userMapFromOauth = JSON.parseObject(userJsonStr, Map.class);
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(uId);
        //检查用户是否存在
        UmsMember umsMembeById = umsMemberService.getOauthUser(umsMember);
        //保存用户信息到数据库（token,code）
        umsMember.setSourceType("2");
        umsMember.setAccessToken(accessToken);
        umsMember.setAccessCode(code);
        umsMember.setCity((String)userMapFromOauth.get("location"));
        umsMember.setNickname((String)userMapFromOauth.get("screen_name"));
        umsMember.setGender((String)userMapFromOauth.get("gender"));
        if(umsMembeById == null){
            umsMemberService.addOauthUser(umsMember);
            umsMembeById = umsMemberService.getOauthUser(umsMember);
        }
        umsMember = umsMembeById;

        String token = null;
        //使用jwt生成token
        Map<String,Object> userMapFromDB = new HashMap<>();
        //密钥
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        userMapFromDB.put("memberId",memberId);
        userMapFromDB.put("nickname",nickname);

        token = getToken("20191111gmall",request,userMapFromDB);
        //重定向到首页
        return "redirect:http://localhost:8084/index?token="+token;
    }
   @RequestMapping("verify")
   @ResponseBody
   public String verify(String token,String currentIp,HttpServletRequest request){
       Map<String,String> map = new HashMap<>();
       //使用jwt，进行验证token,可以获得用户id相关信息
       Map<String, Object> decode = JwtUtil.decode(token, "20191111gmall", currentIp);
       if(decode != null){
          map.put("status","success");
          map.put("memberId", (String) decode.get("memberId"));
          map.put("nickname",(String) decode.get("nickname"));
       }else{
           map.put("status","fail");
       }
       return JSON.toJSONString(map);
   }

    /**
     * @DESC 只负责颁发token,和验证token
     * @param umsMember
     * @return
     */

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        String token = "";
        //用户名和密码验证
        UmsMember umsMember1 = umsMemberService.login(umsMember);
        if(umsMember1 != null){
            //验证成功颁发由jwt生成一个token
            //私有部分
            Map<String,Object> userMap = new HashMap<>();
            String memberId = umsMember1.getId();
            String nickname = umsMember1.getNickname();
            userMap.put("memberId",memberId);
            userMap.put("nickname",nickname);
            //salt+ip
            token = getToken("20191111gmall", request, userMap);
            //将token加入缓冲（redis）
            umsMemberService.addUserToken(token,memberId);
        }else {
            //验证失败，返回空
            token = "fail";
        }
        System.out.println(token);
        return token;
    }

    private String getToken(String key,HttpServletRequest request, Map<String, Object> userMap) {
       String token = null;
        //由于负载均衡,获取用户ip
        String ip = request.getHeader("x-forwarded-for");
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();
            if(StringUtils.isBlank(ip)){
                //这里应该进行异常处理，暂时无法操作
                ip = "127.0.0.1";
            }
        }
        //key和ip须要通过加密算法，再生成token
        token = JwtUtil.encode(key,userMap,ip);
        return  token;
    }

    @RequestMapping("index")
    public String index(String returnURL, ModelMap modelMap){
        modelMap.put("returnURL",returnURL);
        return "index";
    }
}
