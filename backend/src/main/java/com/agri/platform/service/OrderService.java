package com.agri.platform.service;

import com.agri.platform.entity.OrderMaster;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<OrderMaster> {
    OrderMaster createOrder(OrderMaster order, Long productId, Integer quantity);
    boolean deductProductStock(Long productId, Integer quantity);
    List<Map<String, Object>> getBuyerOrders(Long buyerId);
    boolean shipOrder(Long orderId, String trackingNo);
    boolean applyRefund(Long orderId, String reason);
}
