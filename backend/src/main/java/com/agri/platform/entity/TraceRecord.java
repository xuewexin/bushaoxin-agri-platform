package com.agri.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("tb_trace_record")
public class TraceRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceCode;
    private Long productId;
    private String nodeName;
    private String nodeTime;
    private String operator;
    private String description;
}
