package com.agri.platform.controller;

import com.agri.platform.common.Result;
import com.agri.platform.entity.WholesaleInquiry;
import com.agri.platform.service.WholesaleInquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wholesale")
public class WholesaleController {

    @Autowired
    private WholesaleInquiryService wholesaleInquiryService;

    @PostMapping("/inquiry")
    public Result<WholesaleInquiry> submitInquiry(@RequestBody Map<String, Object> body) {
        try {
            WholesaleInquiry inquiry = new WholesaleInquiry();
            inquiry.setBuyerId(Long.valueOf(body.getOrDefault("buyerId", 5).toString()));
            inquiry.setProductId(Long.valueOf(body.get("productId").toString()));
            inquiry.setDemandQuantity(new BigDecimal(body.get("demandQuantity").toString()));
            inquiry.setUnit((String) body.getOrDefault("unit", "吨"));
            inquiry.setExpectedUnitPrice(new BigDecimal(body.get("expectedUnitPrice").toString()));
            inquiry.setDeliveryTargetDate(LocalDate.parse(body.get("deliveryTargetDate").toString()));
            inquiry.setPackagingDemand((String) body.getOrDefault("packagingDemand", "标准加厚箱冷链直达"));

            WholesaleInquiry saved = wholesaleInquiryService.createInquiry(inquiry);
            return Result.success("大宗询价单已生成并推送给合作社！", saved);
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    @PostMapping("/coop-response")
    public Result<Boolean> coopResponse(@RequestBody Map<String, Object> body) {
        Long inquiryId = Long.valueOf(body.get("inquiryId").toString());
        boolean accept = Boolean.parseBoolean(body.get("accept").toString());
        BigDecimal quotePrice = body.get("quotePrice") != null 
                ? new BigDecimal(body.get("quotePrice").toString()) : null;

        boolean ok = wholesaleInquiryService.coopQuoteOrAccept(inquiryId, quotePrice, accept);
        return Result.success(ok ? "操作成功" : "处理失败", ok);
    }

    @PostMapping("/generate-contract")
    public Result<Map<String, Object>> generateContract(@RequestBody Map<String, Object> body) {
        try {
            Long inquiryId = Long.valueOf(body.get("inquiryId").toString());
            Map<String, Object> contract = wholesaleInquiryService.generateContract(inquiryId);
            return Result.success("电子意向合同已生成并加盖防伪时间戳！", contract);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my-inquiries")
    public Result<List<Map<String, Object>>> getMyInquiries(
            @RequestParam Long userId,
            @RequestParam String roleType) {
        List<Map<String, Object>> list = wholesaleInquiryService.getInquiryList(userId, roleType);
        return Result.success(list);
    }
}
