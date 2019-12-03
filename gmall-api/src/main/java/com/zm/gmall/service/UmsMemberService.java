package com.zm.gmall.service;


import com.zm.gmall.bean.UmsMember;
import com.zm.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UmsMemberService {
    public List<UmsMember> getUserList();
    public int addUser(UmsMember umsMember);
    public int removeUser(String id);
    public int modifyUser(UmsMember umsMember);
    public UmsMember login(UmsMember umsMember);
    public void addUserToken(String token,String memberId);
    public void addOauthUser(UmsMember umsMember);

    public UmsMember getOauthUser(UmsMember umsMember);
    public List<UmsMemberReceiveAddress>  getReceiveAddrByMemberId(String memberId);

    UmsMemberReceiveAddress getReceiveAddrById(String recieveAddressId);

    UmsMember getUmsMemberById(String memberId);
}
