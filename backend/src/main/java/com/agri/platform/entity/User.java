package com.agri.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_user")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String roleType; // COOP, ANCHOR, WHOLESALE, CONSUMER, ADMIN
    private String avatar;
    private Integer auditStatus; // 0-待审核, 1-已认证, 2-驳回
    private LocalDateTime createTime;
}
