package com.zm.gmall.interceptors;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.zm.gmall.annotations.LoginRequired;
import com.zm.gmall.util.CookieUtil;
import com.zm.gmall.util.HttpClientUtil;
import org.springframework.stereotype.Component;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{


        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            HandlerMethod methodHandle = (HandlerMethod) handler;
            LoginRequired methodAnnotation = methodHandle.getMethodAnnotation(LoginRequired.class);
            StringBuffer requestURL1 = request.getRequestURL();
            System.out.println(requestURL1);
            //判断token是否有值
            String token = "";
            //判断该请求是否要别拦截
            if(methodAnnotation == null){
                return true;
            }else {

                //获取cookie中的token
                String oldToken = CookieUtil.getCookieValue(request,"oldToken",true);
                if(!StringUtils.isBlank(oldToken)){
                    token = oldToken;
                }
                //获取请求中的token
                String newToken = request.getParameter("token");
                if(!StringUtils.isBlank(newToken)){
                    token = newToken;
                }

                String success = "fail";

                Map<String,String> map = new HashMap<>();
                String ip = request.getHeader("x-forwarded-for");
                if(StringUtils.isBlank(ip)){
                    ip = request.getRemoteAddr();
                    if(StringUtils.isBlank(ip)){
                        //这里应该进行异常处理，暂时无法操作
                        ip = "127.0.0.1";
                    }
                }
                //通过一个认证中心认证,验证token
                if(!StringUtils.isBlank(token)){
                    String jsonStr = HttpClientUtil.doGet("http://localhost:8086/verify?token="+token+"&currentIp="+ip);
                    map = JSON.parseObject(jsonStr, map.getClass());
                    success = map.get("status");
                }
                //判断是否必须通过拦截器
                boolean b = methodAnnotation.loginSuccess();

                if (b) {
                    //needValue == true:必须验证登录的情况下
                    //必须拦截
                    //判断验证中心是否通过
                    if(!"success".equals(success)){
                        //验证失败
                        //重定向到登录界面
                        StringBuffer requestURL = request.getRequestURL();
                        response.sendRedirect("http://localhost:8086/index?returnURL="+requestURL);
                        return false;
                    }
                    //验证成功，携带用户信息传过去，使用用户服务
                    request.setAttribute("memberId",map.get("memberId"));
                    request.setAttribute("nickname",map.get("nickname"));

                    //把cookie保存客户端
                    if(!StringUtils.isBlank(token)) {
                        CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                    }
                }else{
                    //并不需要强制拦截
                    if("success".equals(success)){
                        //验证成功，携带用户信息传过去
                        request.setAttribute("memberId",map.get("memberId"));
                        request.setAttribute("nickname",map.get("nickname"));
                        //把cookie保存客户端
                        if(!StringUtils.isBlank(token)) {
                            CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                        }
                    }
                }
            }

            return true;
        }
}
