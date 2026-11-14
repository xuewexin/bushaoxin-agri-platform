package com.agri.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_cooperative")
public class Cooperative implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String coopName;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Double longitude;
    private Double latitude;
    private Double baseArea;
    private String licenseUrl;
    private String creditCode;
    private String description;
    private LocalDateTime createTime;
}
