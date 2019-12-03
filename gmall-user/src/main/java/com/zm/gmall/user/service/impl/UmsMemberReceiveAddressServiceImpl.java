package com.zm.gmall.user.service.impl;


import com.zm.gmall.bean.UmsMemberReceiveAddress;
import com.zm.gmall.user.dao.UmsMemberReceiveAddressMapper;
import com.zm.gmall.user.service.UmsMemberReceiveAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
