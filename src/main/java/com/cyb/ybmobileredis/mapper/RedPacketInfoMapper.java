package com.cyb.ybmobileredis.mapper;

import com.cyb.ybmobileredis.domain.RedPacketInfo;

import java.util.List;

public interface RedPacketInfoMapper {
    List<RedPacketInfo> ListRedPacketInfo();
    void insert(RedPacketInfo redPacketInfo);
}
