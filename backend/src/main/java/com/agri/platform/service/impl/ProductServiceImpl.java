package com.agri.platform.service.impl;

import com.agri.platform.entity.OriginArchive;
import com.agri.platform.entity.Product;
import com.agri.platform.entity.TraceRecord;
import com.agri.platform.mapper.OriginArchiveMapper;
import com.agri.platform.mapper.ProductMapper;
import com.agri.platform.mapper.TraceRecordMapper;
import com.agri.platform.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private OriginArchiveMapper originArchiveMapper;

    @Autowired
    private TraceRecordMapper traceRecordMapper;

    @Override
    public List<Product> queryProducts(String keyword, String category, String originPlace, Integer isPresale) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getTitle, keyword).or().like(Product::getOriginPlace, keyword));
        }
        if (StringUtils.hasText(category) && !"全部".equals(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        if (StringUtils.hasText(originPlace)) {
            wrapper.like(Product::getOriginPlace, originPlace);
        }
        if (isPresale != null) {
            wrapper.eq(Product::getIsPresale, isPresale);
        }
        wrapper.orderByDesc(Product::getId);
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> getProductDetail(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在！");
        }
        LambdaQueryWrapper<OriginArchive> archiveWrapper = new LambdaQueryWrapper<>();
        archiveWrapper.eq(OriginArchive::getProductId, productId);
        OriginArchive archive = originArchiveMapper.selectOne(archiveWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("archive", archive);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long quickPublish(Product product, OriginArchive archive) {
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getVersion() == null) {
            product.setVersion(0);
        }
        this.save(product);

        if (archive != null) {
            archive.setProductId(product.getId());
            originArchiveMapper.insert(archive);
        }

        // 初始化一条第一阶段溯源记录
        TraceRecord record = new TraceRecord();
        record.setProductId(product.getId());
        record.setTraceCode("AGRI" + System.currentTimeMillis());
        record.setNodeName("产地货源上线备案");
        record.setNodeTime("刚刚");
        record.setOperator("种植合作社负责人");
        record.setDescription("当季农产品审核通过，正式录入数字化产销对接平台货架。");
        traceRecordMapper.insert(record);

        return product.getId();
    }

    @Override
    public boolean updateProductStock(Long productId, Integer newStock) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new RuntimeException("商品未找到！");
        }
        product.setStock(newStock);
        return this.updateById(product);
    }

    @Override
    public List<TraceRecord> getTraceRecords(String traceCode, Long productId) {
        LambdaQueryWrapper<TraceRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(traceCode)) {
            wrapper.eq(TraceRecord::getTraceCode, traceCode);
        } else if (productId != null) {
            wrapper.eq(TraceRecord::getProductId, productId);
        }
        wrapper.orderByAsc(TraceRecord::getId);
        return traceRecordMapper.selectList(wrapper);
    }

    @Override
    public List<Product> getCoopProducts(Long coopId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCoopId, coopId);
        wrapper.orderByDesc(Product::getId);
        return this.list(wrapper);
    }
}
