package com.agri.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("tb_origin_archive")
public class OriginArchive implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String soilQuality;
    private String waterEnvironment;
    private String fieldPhotos; // JSON 格式图片列表
    private String pesticideTestReport;
    private String testResult;
    private String originStory;
}
