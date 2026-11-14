package com.agri.platform.controller;

import com.agri.platform.common.Result;
import com.agri.platform.entity.User;
import com.agri.platform.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        try {
            User user = userService.login(username, password);
            Map<String, Object> resp = new HashMap<>();
            resp.put("user", user);
            resp.put("token", "JWT_" + user.getRoleType() + "_" + user.getId() + "_" + System.currentTimeMillis());
            return Result.success("登录成功，欢迎回来！", resp);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String realName = body.get("realName");
        String phone = body.get("phone");
        String roleType = body.getOrDefault("roleType", "CONSUMER");

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Result.error("账号或密码不能为空！");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userService.count(wrapper) > 0) {
            return Result.error("该账号用户名已被注册，请更换！");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(StringUtils.hasText(realName) ? realName : username);
        user.setPhone(StringUtils.hasText(phone) ? phone : "1380000" + (int)(Math.random()*9000 + 1000));
        user.setRoleType(roleType);
        user.setAuditStatus(1);
        user.setCreateTime(LocalDateTime.now());
        
        // 赋予不同角色的默认头像
        if ("COOP".equals(roleType)) {
            user.setAvatar("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120");
        } else if ("ANCHOR".equals(roleType)) {
            user.setAvatar("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120");
        } else if ("WHOLESALE".equals(roleType)) {
            user.setAvatar("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=120");
        } else if ("ADMIN".equals(roleType)) {
            user.setAvatar("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120");
        } else if ("EXPERT".equals(roleType)) {
            user.setAvatar("https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=120");
        } else {
            user.setAvatar("https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=120");
        }

        userService.save(user);

        Map<String, Object> resp = new HashMap<>();
        resp.put("user", user);
        resp.put("token", "JWT_" + user.getRoleType() + "_" + user.getId() + "_" + System.currentTimeMillis());
        return Result.success("恭喜您，注册成功并已自动登录！", resp);
    }

    @PostMapping("/switch-role")
    public Result<Map<String, Object>> switchRole(@RequestBody Map<String, String> body) {
        String roleType = body.get("roleType");
        User user = userService.switchRole(roleType);
        Map<String, Object> resp = new HashMap<>();
        resp.put("user", user);
        resp.put("token", "JWT_" + user.getRoleType() + "_" + user.getId() + "_" + System.currentTimeMillis());
        return Result.success("已成功切换为身份：" + roleType, resp);
    }
}
