package com.zm.gmall.user.service.impl;


import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;

import com.zm.gmall.bean.UmsMember;
import com.zm.gmall.bean.UmsMemberReceiveAddress;
import com.zm.gmall.service.UmsMemberService;
import com.zm.gmall.user.dao.UmsMemberMapper;
import com.zm.gmall.user.dao.UmsMemberReceiveAddressMapper;
import com.zm.gmall.user.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;
import java.util.UUID;

@Service
public class UmsMemberImpl implements UmsMemberService {
    @Autowired
    UmsMemberMapper umsMemberMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> getUserList() {
        return umsMemberMapper.selectUser();
    }

    @Override
    public int addUser(UmsMember umsMember) {
        return umsMemberMapper.insertUser(umsMember);
    }

    @Override
    public int removeUser(String id) {
        return umsMemberMapper.deleteUserById(id);
    }

    @Override
    public int modifyUser(UmsMember umsMember) {
        return umsMemberMapper.updateUser(umsMember);
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        //获取redis的连接对象
        Jedis jedis = null;
        //判断是否获取jedis
        try {
           jedis = redisUtil.getJedis();
            String key = null;
            if(jedis != null) {
                //查询缓冲中的数据
                key = "user:" + umsMember.getUsername() + umsMember.getPassword() + "info";
                String value = jedis.get(key);
                if (!StringUtils.isBlank(value)) {
                    //json转成java对象
                    UmsMember umsMemberFromCache = JSON.parseObject(value, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            //查询数据库
            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String lock = "user:"+umsMember.getUsername() + umsMember.getPassword()+"lock";
            String set = jedis.set(lock, token, "nx", "px", 1000);
            if(!StringUtils.isBlank(set)&&"OK".equals(set)){
                 UmsMember umsMemberFromDB = getUmsMemberFromDB(umsMember);
                 //更新缓冲中的数据
                 if(umsMemberFromDB != null){
                        jedis.setex(key,60*60*24,JSON.toJSONString(umsMemberFromDB));
                 }else{
                     jedis.setex(key,60*60*24,JSON.toJSONString(""));
                 }
                 //使用完锁，进行释放
                String newToken = jedis.get(lock);
                 if(!StringUtils.isBlank(newToken)&&token.equals(newToken)) {
                     jedis.del(lock);
                 }
                return umsMemberFromDB;
            }else {
                // 同步请求时，没有获得分布式锁，进行自旋操作
                return login(umsMember);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    
        return null;
    }

    @Override
    public void addUserToken(String token,String memberId) {
        //获取jedis
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String key = "user:"+memberId+":token";
            jedis.setex(key,60*60*24,JSON.toJSONString(token));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }

    @Override
    public void addOauthUser(UmsMember umsMember) {
        if(umsMember != null){
            umsMemberMapper.insertSelective(umsMember);
        }
    }

    @Override
    public UmsMember getOauthUser(UmsMember umsMember) {
        return umsMemberMapper.selectOne(umsMember);
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddrByMemberId(String memberId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> receiveAddressList = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return receiveAddressList;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddrById(String recieveAddressId) {
        UmsMemberReceiveAddress receiveAddress = new UmsMemberReceiveAddress();
        receiveAddress.setId(recieveAddressId);
        UmsMemberReceiveAddress receiveAddress1 = umsMemberReceiveAddressMapper.selectOne(receiveAddress);

        return receiveAddress1;
    }

    @Override
    public UmsMember getUmsMemberById(String memberId) {
        UmsMember umsMember = new UmsMember();
        umsMember.setId(memberId);
        UmsMember umsMember1 = umsMemberMapper.selectOne(umsMember);
        return umsMember1;
    }

    private UmsMember getUmsMemberFromDB(UmsMember umsMember) {

        List<UmsMember> umsMembers = umsMemberMapper.select(umsMember);
        if(umsMembers != null){
            umsMember = umsMembers.get(0);
            return umsMember;
        }
        return null;
    }


}
