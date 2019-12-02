package com.zm.gmall.manage;



import com.zm.gmall.manage.dao.AttrMapper;
import com.zm.gmall.service.AttrService;
import com.zm.gmall.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import tk.mybatis.spring.annotation.MapperScan;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = {"com.zm.gmall"})
public class GmallManageServiceApplicationTests {

    @Test
    public void contextLoads() {
        String s1 = "39";
        String s2 = "39";
        System.out.println(s1.equals(s2));
        System.out.println(!s1.equals(s2));
    }

}
