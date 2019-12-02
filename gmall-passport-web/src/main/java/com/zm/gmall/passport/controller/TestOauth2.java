package com.zm.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zm.gmall.util.HttpClientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {
    //获取授权码
    public static String getCode(){
        //3917917273
        //17fa10b8e615b31c311300a5336bd6a3
        //http://127.0.0.1:8086/vlogin
        String s1 = HttpClientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=3917917273&response_type=code&redirect_uri=http://127.0.0.1:8086/vlogin");
        System.out.println(s1);
        //7bf86dd73e3f935ebc64f001da73f4fe(授权码)
        String s2 = "http://127.0.0.1:8086/vlogin?code=7bf86dd73e3f935ebc64f001da73f4fe";
        return null;
    }
    //获取access-token
    public static Map<String,String> getAccessToken(){
        //通过授权码，获取access_token
        String s3 = "https://api.weibo.com/oauth2/access_token?";//client_id=3917917273&client_secret=17fa10b8e615b31c311300a5336bd6a3&grant_type=authorization_code&redirect_uri=http://127.0.0.1:8086/vlogin&code=7bf86dd73e3f935ebc64f001da73f4fe";

        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","3917917273");
        paramMap.put("client_secret","17fa10b8e615b31c311300a5336bd6a3");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://127.0.0.1:8086/vlogin");
        paramMap.put("code","85274896fa2a852c996d7f4e23f3353f");

        String access_token_json = HttpClientUtil.doPost(s3,paramMap);
        //通过access_token，获取用户信息
        Map<String,String> object = JSON.parseObject(access_token_json,Map.class);
        return object;
    }
    public static Map<String,String> getUserInfo(){
        String userStr = HttpClientUtil.doGet("https://api.weibo.com/2/users/show.json?access_token=2.00TbXfCHFQLJRE6fe5973f62wju2lD&uid=1");
        Map<String,String> map = JSON.parseObject(userStr, Map.class);
        return map;
    }
    public static void main(String[] args) {
            getCode();

    }
}
