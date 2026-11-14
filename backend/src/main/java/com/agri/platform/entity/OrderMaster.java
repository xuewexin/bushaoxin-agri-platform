package com.agri.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_order")
public class OrderMaster implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long buyerId;
    private Integer orderType; // 1-零售直发, 2-直播秒杀, 3-大宗直采
    private BigDecimal totalAmount;
    private Integer payStatus; // 0-未支付, 1-已支付
    private Integer orderStatus; // 0-待付款, 1-待发货, 2-已发货, 3-已完成, 4-退款中
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String trackingNo;
    private LocalDateTime createTime;
}
