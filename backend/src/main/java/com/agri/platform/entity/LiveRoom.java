package com.agri.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_live_room")
public class LiveRoom implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long anchorId;
    private String title;
    private String coverUrl;
    private String streamUrl;
    private Long currentProductId;
    private Integer status; // 0-未开播, 1-直播中, 2-已下播
    private Integer onlineCount;
    private BigDecimal totalGmv;
    private LocalDateTime createTime;
}
