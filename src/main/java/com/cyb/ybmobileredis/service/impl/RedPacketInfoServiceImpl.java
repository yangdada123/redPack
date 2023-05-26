package com.cyb.ybmobileredis.service.impl;

import com.cyb.ybmobileredis.domain.RedPacketInfo;
import com.cyb.ybmobileredis.mapper.RedPacketInfoMapper;
import com.cyb.ybmobileredis.service.RedPacketInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName：RedPacketInfoServiceImpl
 * @Description：红包信息
 * @Author：chenyb
 * @Date：2020/8/24 10:36 下午
 * @Versiion：1.0
 */
@Service
public class RedPacketInfoServiceImpl implements RedPacketInfoService {
    @Autowired
    private RedPacketInfoMapper redPacketInfoMapper;

    /**
     * 添加红包
     * @param redPacketInfo
     */
    @Override
    public void insert(RedPacketInfo redPacketInfo) {
        redPacketInfoMapper.insert(redPacketInfo);
    }
}
