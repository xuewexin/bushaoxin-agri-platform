package com.agri.platform.service.impl;

import com.agri.platform.entity.LiveRoom;
import com.agri.platform.entity.OriginArchive;
import com.agri.platform.entity.Product;
import com.agri.platform.mapper.LiveRoomMapper;
import com.agri.platform.mapper.OriginArchiveMapper;
import com.agri.platform.mapper.ProductMapper;
import com.agri.platform.service.LiveRoomService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom> implements LiveRoomService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OriginArchiveMapper originArchiveMapper;

    @Override
    public Map<String, Object> getLiveDetail(Long liveId) {
        LiveRoom room = this.getById(liveId);
        if (room == null) {
            throw new RuntimeException("直播间不存在！");
        }
        // 获取挂载的所有在售农产品 (小黄车货架)
        LambdaQueryWrapper<Product> prodWrapper = new LambdaQueryWrapper<>();
        prodWrapper.eq(Product::getStatus, 1).orderByDesc(Product::getId);
        List<Product> products = productMapper.selectList(prodWrapper);

        // 获取当前主推商品详情及产地质检
        Product currentProduct = null;
        OriginArchive archive = null;
        if (room.getCurrentProductId() != null) {
            currentProduct = productMapper.selectById(room.getCurrentProductId());
            if (currentProduct != null) {
                LambdaQueryWrapper<OriginArchive> arcWrapper = new LambdaQueryWrapper<>();
                arcWrapper.eq(OriginArchive::getProductId, currentProduct.getId());
                archive = originArchiveMapper.selectOne(arcWrapper);
            }
        } else if (!products.isEmpty()) {
            currentProduct = products.get(0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("room", room);
        result.put("products", products);
        result.put("currentProduct", currentProduct);
        result.put("originArchive", archive);
        return result;
    }

    @Override
    public boolean switchProduct(Long liveId, Long productId) {
        LiveRoom room = this.getById(liveId);
        if (room == null) return false;
        room.setCurrentProductId(productId);
        return this.updateById(room);
    }

    @Override
    public boolean changeLiveStatus(Long liveId, Integer status) {
        LiveRoom room = this.getById(liveId);
        if (room == null) return false;
        room.setStatus(status);
        return this.updateById(room);
    }

    @Override
    public Map<String, Object> getLiveReport(Long liveId) {
        LiveRoom room = this.getById(liveId);
        Map<String, Object> report = new HashMap<>();
        report.put("title", room != null ? room.getTitle() : "产地带货实况");
        report.put("totalGmv", room != null ? room.getTotalGmv() : new BigDecimal("68900.00"));
        report.put("onlineCount", room != null ? room.getOnlineCount() : 3520);
        report.put("orderCount", 128); // 场次订单数
        report.put("conversionRate", "8.7%");
        report.put("topProduct", "四川凉山盐源丑苹果 (已成交92单)");
        return report;
    }
}
