package com.agri.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_product")
public class Product implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long coopId;
    private String title;
    private String category;
    private String originPlace;
    private BigDecimal retailPrice;
    private BigDecimal wholesalePrice;
    private Integer minWholesaleAmount;
    private Integer stock;
    private String unit;
    private String yieldCycle;
    private String totalYield;
    private String coverImage;
    private Integer isPresale; // 0-现货直发, 1-田间预售
    private LocalDate presaleDeliveryDate;
    private Integer status; // 0-下架, 1-在售, 2-封禁
    
    @Version
    private Integer version; // 乐观锁版本号
    private LocalDateTime createTime;
}
