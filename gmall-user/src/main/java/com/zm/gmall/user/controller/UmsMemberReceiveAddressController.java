package com.zm.gmall.user.controller;




import com.zm.gmall.bean.UmsMemberReceiveAddress;
import com.zm.gmall.user.service.UmsMemberReceiveAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.List;

@Controller
public class UmsMemberReceiveAddressController {
    @Autowired
    UmsMemberReceiveAddressService umsMemberReceiveAddressService;

    @RequestMapping("/getMember")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getMember(){

        return umsMemberReceiveAddressService.getUmsMemberReceiveAddress();
    }
}
