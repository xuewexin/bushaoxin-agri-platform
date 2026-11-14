package com.agri.platform.service;

import com.agri.platform.entity.LiveRoom;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface LiveRoomService extends IService<LiveRoom> {
    Map<String, Object> getLiveDetail(Long liveId);
    boolean switchProduct(Long liveId, Long productId);
    boolean changeLiveStatus(Long liveId, Integer status);
    Map<String, Object> getLiveReport(Long liveId);
}
