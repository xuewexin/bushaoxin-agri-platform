package com.agri.platform.service;

import com.agri.platform.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    User login(String username, String password);
    User switchRole(String roleType);
}
