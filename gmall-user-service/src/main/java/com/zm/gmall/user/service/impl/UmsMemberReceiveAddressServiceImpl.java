package com.zm.gmall.user.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.zm.gmall.bean.UmsMemberReceiveAddress;
import com.zm.gmall.service.UmsMemberReceiveAddressService;
import com.zm.gmall.user.dao.UmsMemberReceiveAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

@Service
public class UmsMemberReceiveAddressServiceImpl implements UmsMemberReceiveAddressService {
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMemberReceiveAddress> getUmsMemberReceiveAddress() {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        return this.umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
    }
}
