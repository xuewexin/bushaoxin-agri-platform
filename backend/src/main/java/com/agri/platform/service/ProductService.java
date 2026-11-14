package com.agri.platform.service;

import com.agri.platform.entity.OriginArchive;
import com.agri.platform.entity.Product;
import com.agri.platform.entity.TraceRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {
    List<Product> queryProducts(String keyword, String category, String originPlace, Integer isPresale);
    Map<String, Object> getProductDetail(Long productId);
    Long quickPublish(Product product, OriginArchive archive);
    boolean updateProductStock(Long productId, Integer newStock);
    List<TraceRecord> getTraceRecords(String traceCode, Long productId);
    List<Product> getCoopProducts(Long coopId);
}
