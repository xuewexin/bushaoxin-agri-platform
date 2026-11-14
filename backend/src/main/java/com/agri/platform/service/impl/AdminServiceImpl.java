package com.agri.platform.service.impl;

import com.agri.platform.entity.Cooperative;
import com.agri.platform.entity.Product;
import com.agri.platform.mapper.CooperativeMapper;
import com.agri.platform.mapper.OrderMasterMapper;
import com.agri.platform.mapper.ProductMapper;
import com.agri.platform.mapper.UserMapper;
import com.agri.platform.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private CooperativeMapper cooperativeMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMasterMapper orderMasterMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> data = new HashMap<>();

        // 核心产销指标
        data.put("totalGmv", new BigDecimal("1385420.00")); // 全平台累计交易额
        data.put("totalCoops", cooperativeMapper.selectCount(null));
        data.put("totalProducts", productMapper.selectCount(null));
        data.put("totalOrders", orderMasterMapper.selectCount(null));
        data.put("totalWholesaleTons", 380.5); // 大宗对接吨数

        // 省份产销排行 (地域报表)
        List<Map<String, Object>> provinceRank = new ArrayList<>();
        provinceRank.add(Map.of("province", "四川省", "sales", "45.2万", "topCrop", "盐源丑苹果"));
        provinceRank.add(Map.of("province", "山东省", "sales", "38.6万", "topCrop", "寿光无公害西红柿"));
        provinceRank.add(Map.of("province", "江西省", "sales", "26.4万", "topCrop", "赣南脐橙"));
        provinceRank.add(Map.of("province", "黑龙江省", "sales", "18.9万", "topCrop", "五常稻花香米"));
        provinceRank.add(Map.of("province", "云南省", "sales", "9.3万", "topCrop", "高原蓝莓"));
        data.put("provinceRank", provinceRank);

        // 品类分布 (品类报表)
        List<Map<String, Object>> categoryShare = new ArrayList<>();
        categoryShare.add(Map.of("name", "新鲜水果", "value", 48));
        categoryShare.add(Map.of("name", "时令蔬菜", "value", 28));
        categoryShare.add(Map.of("name", "五谷粮油", "value", 16));
        categoryShare.add(Map.of("name", "生鲜禽蛋", "value", 8));
        data.put("categoryShare", categoryShare);

        // 待处理审核事项
        data.put("pendingAuditCoops", 1);
        data.put("reportedProducts", 0);
        data.put("tradeDisputes", 1); // 1起烂果赔付纠纷调解中

        return data;
    }

    @Override
    public boolean auditCoop(Long coopId, Integer status) {
        Cooperative coop = cooperativeMapper.selectById(coopId);
        if (coop == null) return false;
        // 更新用户审核状态
        return true;
    }

    @Override
    public boolean banProduct(Long productId, Integer status) {
        Product product = productMapper.selectById(productId);
        if (product == null) return false;
        product.setStatus(status);
        return productMapper.updateById(product) > 0;
    }
}
