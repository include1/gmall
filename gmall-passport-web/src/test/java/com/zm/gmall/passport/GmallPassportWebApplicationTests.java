package com.zm.gmall.passport;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void testJwt() {
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode("eyJuaWNrbmFtZSI6ImRkIiwibWVtYmVySWQiOiIxMiJ9");//填写用户的私钥
        String tokenJson = null;
        try {
            tokenJson  = new String(decode, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String,String> map = JSON.parseObject(tokenJson, Map.class);
        System.out.println(map);
    }

}
