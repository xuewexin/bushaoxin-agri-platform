package com.agri.platform.controller;

import com.agri.platform.common.Result;
import com.agri.platform.entity.OrderMaster;
import com.agri.platform.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<OrderMaster> createOrder(@RequestBody Map<String, Object> body) {
        try {
            Long buyerId = Long.valueOf(body.getOrDefault("buyerId", 6).toString());
            Long productId = Long.valueOf(body.get("productId").toString());
            Integer quantity = Integer.valueOf(body.getOrDefault("quantity", 1).toString());
            Integer orderType = Integer.valueOf(body.getOrDefault("orderType", 1).toString());

            OrderMaster order = new OrderMaster();
            order.setBuyerId(buyerId);
            order.setOrderType(orderType);
            order.setReceiverName((String) body.getOrDefault("receiverName", "张小敏"));
            order.setReceiverPhone((String) body.getOrDefault("receiverPhone", "18612345678"));
            order.setReceiverAddress((String) body.getOrDefault("receiverAddress", "北京市朝阳区望京科技大厦B座1201室"));

            OrderMaster result = orderService.createOrder(order, productId, quantity);
            return Result.success("下单支付成功！产地已接收到订单并准备发货", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public Result<List<Map<String, Object>>> getMyOrders(@RequestParam(required = false) Long buyerId) {
        return Result.success(orderService.getBuyerOrders(buyerId));
    }

    @PostMapping("/ship")
    public Result<Boolean> shipOrder(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String trackingNo = (String) body.getOrDefault("trackingNo", "SF" + System.currentTimeMillis());
        boolean ok = orderService.shipOrder(orderId, trackingNo);
        return Result.success("发货成功，冷链物流已录入", ok);
    }

    @PostMapping("/refund")
    public Result<Boolean> refund(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String reason = (String) body.getOrDefault("reason", "生鲜磕碰/品质异议");
        boolean ok = orderService.applyRefund(orderId, reason);
        return Result.success("售后申请已受理，进入极速退款审核流程", ok);
    }
}
