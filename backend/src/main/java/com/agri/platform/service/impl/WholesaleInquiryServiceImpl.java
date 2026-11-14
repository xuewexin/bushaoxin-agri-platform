package com.agri.platform.service.impl;

import com.agri.platform.entity.Cooperative;
import com.agri.platform.entity.Product;
import com.agri.platform.entity.User;
import com.agri.platform.entity.WholesaleInquiry;
import com.agri.platform.mapper.CooperativeMapper;
import com.agri.platform.mapper.ProductMapper;
import com.agri.platform.mapper.UserMapper;
import com.agri.platform.mapper.WholesaleInquiryMapper;
import com.agri.platform.service.WholesaleInquiryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WholesaleInquiryServiceImpl extends ServiceImpl<WholesaleInquiryMapper, WholesaleInquiry> implements WholesaleInquiryService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CooperativeMapper cooperativeMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public WholesaleInquiry createInquiry(WholesaleInquiry inquiry) {
        Product product = productMapper.selectById(inquiry.getProductId());
        if (product == null) {
            throw new RuntimeException("所询农产品不存在！");
        }
        inquiry.setCoopId(product.getCoopId());
        inquiry.setInquiryNo("INQ" + System.currentTimeMillis());
        inquiry.setStatus(0); // 待合作社确认
        inquiry.setCreateTime(LocalDateTime.now());
        this.save(inquiry);
        return inquiry;
    }

    @Override
    public boolean coopQuoteOrAccept(Long inquiryId, BigDecimal quotePrice, boolean accept) {
        WholesaleInquiry inquiry = this.getById(inquiryId);
        if (inquiry == null) return false;

        if (accept) {
            inquiry.setStatus(1); // 达成意向
            if (quotePrice != null) {
                inquiry.setCoopQuotePrice(quotePrice);
            } else {
                inquiry.setCoopQuotePrice(inquiry.getExpectedUnitPrice());
            }
        } else {
            if (quotePrice != null) {
                inquiry.setCoopQuotePrice(quotePrice);
                // 合作社还价
            } else {
                inquiry.setStatus(2); // 驳回
            }
        }
        return this.updateById(inquiry);
    }

    @Override
    public Map<String, Object> generateContract(Long inquiryId) {
        WholesaleInquiry inquiry = this.getById(inquiryId);
        if (inquiry == null) throw new RuntimeException("询价单不存在！");

        Product product = productMapper.selectById(inquiry.getProductId());
        Cooperative coop = cooperativeMapper.selectById(inquiry.getCoopId());
        User buyer = userMapper.selectById(inquiry.getBuyerId());

        String contractNo = "AGRI-CT-" + System.currentTimeMillis();
        inquiry.setStatus(3); // 已签署生成合同
        inquiry.setContractNo(contractNo);
        this.updateById(inquiry);

        BigDecimal unitPrice = inquiry.getCoopQuotePrice() != null ? inquiry.getCoopQuotePrice() : inquiry.getExpectedUnitPrice();
        BigDecimal totalEstimate = unitPrice.multiply(inquiry.getDemandQuantity()).multiply(new BigDecimal("2000")); // 吨转斤估算

        Map<String, Object> contract = new HashMap<>();
        contract.put("contractNo", contractNo);
        contract.put("inquiryNo", inquiry.getInquiryNo());
        contract.put("buyerName", buyer != null ? buyer.getRealName() : "全国生鲜连锁采购部");
        contract.put("sellerName", coop != null ? coop.getCoopName() : "原产地种植合作社");
        contract.put("productTitle", product != null ? product.getTitle() : "高山特色农产品");
        contract.put("demandQuantity", inquiry.getDemandQuantity() + " " + inquiry.getUnit());
        contract.put("agreedUnitPrice", unitPrice + " 元/斤");
        contract.put("deliveryTargetDate", inquiry.getDeliveryTargetDate());
        contract.put("packagingDemand", inquiry.getPackagingDemand());
        contract.put("signTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        contract.put("legalClause", "供需双方同意遵循国家绿色食品与农产品保供法规，产地直发保质保量，验货通过后付清尾款。");
        return contract;
    }

    @Override
    public List<Map<String, Object>> getInquiryList(Long userId, String roleType) {
        LambdaQueryWrapper<WholesaleInquiry> wrapper = new LambdaQueryWrapper<>();
        if ("WHOLESALE".equalsIgnoreCase(roleType)) {
            wrapper.eq(WholesaleInquiry::getBuyerId, userId);
        } else if ("COOP".equalsIgnoreCase(roleType)) {
            // 查合作社对应的
            Cooperative coop = cooperativeMapper.selectOne(new LambdaQueryWrapper<Cooperative>().eq(Cooperative::getUserId, userId));
            if (coop != null) {
                wrapper.eq(WholesaleInquiry::getCoopId, coop.getId());
            }
        }
        wrapper.orderByDesc(WholesaleInquiry::getId);
        List<WholesaleInquiry> list = this.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WholesaleInquiry item : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("inquiry", item);
            Product product = productMapper.selectById(item.getProductId());
            map.put("product", product);
            Cooperative coop = cooperativeMapper.selectById(item.getCoopId());
            map.put("coop", coop);
            result.add(map);
        }
        return result;
    }
}
