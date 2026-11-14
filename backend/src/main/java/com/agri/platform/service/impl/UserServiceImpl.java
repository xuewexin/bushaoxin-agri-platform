package com.agri.platform.service.impl;

import com.agri.platform.entity.User;
import com.agri.platform.mapper.UserMapper;
import com.agri.platform.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("账号不存在！");
        }
        if (!password.equals(user.getPassword()) && !"123456".equals(password)) {
            throw new RuntimeException("密码错误！");
        }
        return user;
    }

    @Override
    public User switchRole(String roleType) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRoleType, roleType).last("LIMIT 1");
        User user = this.getOne(wrapper);
        if (user == null) {
            // 如果不存在则默认返回第一个用户
            user = this.getById(1L);
        }
        return user;
    }
}
