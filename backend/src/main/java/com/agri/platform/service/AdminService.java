package com.agri.platform.service;

import java.util.Map;

public interface AdminService {
    Map<String, Object> getDashboardStats();
    boolean auditCoop(Long coopId, Integer status);
    boolean banProduct(Long productId, Integer status);
}
