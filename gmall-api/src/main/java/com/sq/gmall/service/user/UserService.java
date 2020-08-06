package com.sq.gmall.service.user;


import com.sq.gmall.bean.UmsMember;
import com.sq.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
