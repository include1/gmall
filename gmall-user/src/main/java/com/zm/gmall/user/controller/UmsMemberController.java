package com.zm.gmall.user.controller;


import com.zm.gmall.service.UmsMemberService;
import com.zm.gmall.bean.UmsMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UmsMemberController {
    @Autowired
    UmsMemberService umsMemberService;

    @RequestMapping("/getUserList")
    @ResponseBody
    public List<UmsMember> getUserList(){
        return umsMemberService.getUserList();
    }
    @RequestMapping("/addUser")
    @ResponseBody
    public String addUser(String username,String password){
        UmsMember user = new UmsMember();
        user.setMemberLevelId("1");
        user.setUsername(username);
        user.setPassword(password);
        if(umsMemberService.addUser(user) > 0){
            return "成功";
        }
        return "增加失败";
    }
    @RequestMapping("/updateUser")
    @ResponseBody
    public String updateUser(String id,String username,String password){
        UmsMember user = new UmsMember();
        user.setUsername(username);
        user.setPassword(password);
        user.setId(id);
        if(umsMemberService.modifyUser(user) > 0){
            return "更新成功";
        }

        return "更新失败";
    }
    @RequestMapping("/deleteUser")
    @ResponseBody
    public String deleteUser(String id){
        if(umsMemberService.removeUser(id) > 0) {
            return "删除成功";
        }
        return "删除失败";
    }
}
