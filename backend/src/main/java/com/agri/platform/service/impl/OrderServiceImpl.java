package com.agri.platform.service.impl;

import com.agri.platform.entity.OrderMaster;
import com.agri.platform.entity.Product;
import com.agri.platform.mapper.OrderMasterMapper;
import com.agri.platform.mapper.ProductMapper;
import com.agri.platform.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMasterMapper, OrderMaster> implements OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderMaster createOrder(OrderMaster order, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }

        // 核心防超卖：乐观锁原子性扣减库存
        boolean stockDeducted = deductProductStock(productId, quantity);
        if (!stockDeducted) {
            throw new RuntimeException("手慢了，该批次货源已被抢空！");
        }

        Product product = productMapper.selectById(productId);
        BigDecimal price = (order.getOrderType() != null && order.getOrderType() == 2) 
                ? product.getRetailPrice().multiply(new BigDecimal("0.9")) // 直播间9折专享
                : product.getRetailPrice();
        BigDecimal total = price.multiply(new BigDecimal(quantity));

        order.setOrderNo("TB" + System.currentTimeMillis());
        order.setTotalAmount(total);
        order.setPayStatus(1); // 模拟付款成功
        order.setOrderStatus(1); // 待发货
        order.setCreateTime(LocalDateTime.now());
        if (order.getTrackingNo() == null) {
            order.setTrackingNo("SF" + System.currentTimeMillis());
        }
        this.save(order);
        return order;
    }

    @Override
    public boolean deductProductStock(Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStock() < quantity) {
            throw new RuntimeException("库存不足！");
        }
        int rows = productMapper.decreaseStock(productId, quantity);
        return rows > 0;
    }

    @Override
    public List<Map<String, Object>> getBuyerOrders(Long buyerId) {
        LambdaQueryWrapper<OrderMaster> wrapper = new LambdaQueryWrapper<>();
        if (buyerId != null) {
            wrapper.eq(OrderMaster::getBuyerId, buyerId);
        }
        wrapper.orderByDesc(OrderMaster::getId);
        List<OrderMaster> orders = this.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderMaster o : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("order", o);
            result.add(map);
        }
        return result;
    }

    @Override
    public boolean shipOrder(Long orderId, String trackingNo) {
        OrderMaster order = this.getById(orderId);
        if (order == null) return false;
        order.setOrderStatus(2); // 已发货
        order.setTrackingNo(trackingNo);
        return this.updateById(order);
    }

    @Override
    public boolean applyRefund(Long orderId, String reason) {
        OrderMaster order = this.getById(orderId);
        if (order == null) return false;
        order.setOrderStatus(4); // 售后退款中
        return this.updateById(order);
    }
}
