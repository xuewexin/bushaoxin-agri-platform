package com.agri.platform.controller;

import com.agri.platform.common.Result;
import com.agri.platform.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(adminService.getDashboardStats());
    }

    @PostMapping("/product-status")
    public Result<Boolean> updateProductStatus(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        boolean ok = adminService.banProduct(productId, status);
        return Result.success(status == 2 ? "违规商品已强制下架" : "已恢复上架", ok);
    }
}
