package com.agri.platform.controller;

import com.agri.platform.common.Result;
import com.agri.platform.entity.OriginArchive;
import com.agri.platform.entity.Product;
import com.agri.platform.entity.TraceRecord;
import com.agri.platform.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<List<Product>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String originPlace,
            @RequestParam(required = false) Integer isPresale) {
        List<Product> list = productService.queryProducts(keyword, category, originPlace, isPresale);
        return Result.success(list);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        try {
            Map<String, Object> detail = productService.getProductDetail(id);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/quick-publish")
    public Result<Long> quickPublish(@RequestBody Map<String, Object> body) {
        try {
            Product product = new Product();
            product.setCoopId(Long.valueOf(body.getOrDefault("coopId", 1).toString()));
            product.setTitle((String) body.get("title"));
            product.setCategory((String) body.get("category"));
            product.setOriginPlace((String) body.get("originPlace"));
            product.setRetailPrice(new java.math.BigDecimal(body.get("retailPrice").toString()));
            product.setWholesalePrice(new java.math.BigDecimal(body.get("wholesalePrice").toString()));
            product.setMinWholesaleAmount(Integer.valueOf(body.getOrDefault("minWholesaleAmount", 500).toString()));
            product.setStock(Integer.valueOf(body.get("stock").toString()));
            product.setUnit((String) body.getOrDefault("unit", "斤"));
            product.setYieldCycle((String) body.get("yieldCycle"));
            product.setTotalYield((String) body.get("totalYield"));
            product.setCoverImage((String) body.getOrDefault("coverImage", "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600"));
            product.setIsPresale(Integer.valueOf(body.getOrDefault("isPresale", 0).toString()));

            OriginArchive archive = new OriginArchive();
            archive.setSoilQuality((String) body.get("soilQuality"));
            archive.setWaterEnvironment((String) body.get("waterEnvironment"));
            archive.setPesticideTestReport((String) body.get("pesticideTestReport"));
            archive.setTestResult((String) body.getOrDefault("testResult", "第三方抽检合格"));
            archive.setOriginStory((String) body.get("originStory"));

            Long productId = productService.quickPublish(product, archive);
            return Result.success("手机端极速上架成功！", productId);
        } catch (Exception e) {
            return Result.error("上架失败: " + e.getMessage());
        }
    }

    @PostMapping("/update-stock")
    public Result<Boolean> updateStock(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer newStock = Integer.valueOf(body.get("newStock").toString());
        boolean success = productService.updateProductStock(productId, newStock);
        return Result.success("库存更新成功", success);
    }

    @GetMapping("/trace")
    public Result<List<TraceRecord>> getTraceRecords(
            @RequestParam(required = false) String traceCode,
            @RequestParam(required = false) Long productId) {
        List<TraceRecord> records = productService.getTraceRecords(traceCode, productId);
        return Result.success(records);
    }

    @GetMapping("/coop-list/{coopId}")
    public Result<List<Product>> getCoopProducts(@PathVariable Long coopId) {
        List<Product> list = productService.getCoopProducts(coopId);
        return Result.success(list);
    }
}
