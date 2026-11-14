package com.agri.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_wholesale_inquiry")
public class WholesaleInquiry implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String inquiryNo;
    private Long buyerId;
    private Long coopId;
    private Long productId;
    private BigDecimal demandQuantity;
    private String unit;
    private BigDecimal expectedUnitPrice;
    private BigDecimal coopQuotePrice;
    private LocalDate deliveryTargetDate;
    private String packagingDemand;
    private Integer status; // 0-待合作社确认, 1-已达成意向, 2-已驳回, 3-已转为合同
    private String contractNo;
    private LocalDateTime createTime;
}
