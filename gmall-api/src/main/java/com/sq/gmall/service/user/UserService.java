package com.sq.gmall.service.user;


import com.sq.gmall.bean.UmsMember;
import com.sq.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    /**
     * 根据用户名密码校验并返回用户数据
     * @param umsMember
     * @return
     */
    UmsMember login(UmsMember umsMember);

    void addUserToken(String token,String memberId);

    /**
     * 添加用户
     * @param umsMember
     */
    UmsMember addUser(UmsMember umsMember);

    /**
     * 根据SourceUid查询用户数据
     * @param check
     * @return
     */
    UmsMember checkOauthUser(UmsMember check);

    /**
     * 根据id查询收货信息
     * @param receiveAddressId
     * @return
     */
    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
