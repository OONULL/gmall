package com.sq.gmall.user.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.sq.gmall.bean.UmsMember;
import com.sq.gmall.bean.UmsMemberReceiveAddress;
import com.sq.gmall.service.user.UserService;
import com.sq.gmall.user.mapper.UmsMemberMapper;
import com.sq.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.sq.gmall.user.mapper.UserMapper;
import com.sq.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
    @Autowired
    private UmsMemberMapper umsMemberMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> umsMembers = userMapper.selectAll();//userMapper.selectAllUser();

        return umsMembers;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        // 封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);


//       Example example = new Example(UmsMemberReceiveAddress.class);
//       example.createCriteria().andEqualTo("memberId",memberId);
//       List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(example);

        return umsMemberReceiveAddresses;
    }

    /**
     * 根据用户名密码校验并返回用户数据
     * @param umsMember
     * @return
     */
    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis =null;
        try {
            jedis = redisUtil.getJedis();
            if(jedis!=null){
                String umsMemberStr = jedis.get("user:" + umsMember.getUsername() + umsMember.getPassword() + ":info");
                if(StringUtils.isNotBlank(umsMemberStr)){
                    //密码正确
                    UmsMember umsMember1 = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMember1;
                }
            }
                //连接redis失败或redis中无数据,开启数据库
                UmsMember umsMemberFromDb = loginFromDb(umsMember);
                if(!org.springframework.util.StringUtils.isEmpty(umsMemberFromDb)){
                    jedis.setex("user:" +umsMember.getUsername() + umsMember.getPassword() + ":info",60*60*24,JSON.toJSONString(umsMemberFromDb));
                }
                return umsMemberFromDb;
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * 将token存入redis
     * @param token
     * @param memberId
     */
    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:" + memberId + ":token",60*60*2, token);

        jedis.close();
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> umsMemberList = umsMemberMapper.select(umsMember);
        if(!CollectionUtils.isEmpty(umsMemberList)){
            return umsMemberList.get(0);
        }
        return null;
    }

    /**
     * 添加用户
     * @param umsMember
     */
    @Override
    public UmsMember addUser(UmsMember umsMember) {
        umsMemberMapper.insertSelective(umsMember);
        return umsMember;
    }

    /**
     * 根据SourceUid查询用户数据
     * @param check
     * @return
     */
    @Override
    public UmsMember checkOauthUser(UmsMember check) {
        List<UmsMember> umsMemberList = umsMemberMapper.select(check);
        if(!CollectionUtils.isEmpty(umsMemberList)){
            return umsMemberList.get(0);
        }
        return null;
    }

    /**
     * 根据id查询收货信息
     * @param receiveAddressId
     * @return
     */
    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {

        return umsMemberReceiveAddressMapper.selectByPrimaryKey(receiveAddressId);
    }
}
