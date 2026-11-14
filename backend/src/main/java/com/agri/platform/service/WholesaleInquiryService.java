package com.agri.platform.service;

import com.agri.platform.entity.WholesaleInquiry;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WholesaleInquiryService extends IService<WholesaleInquiry> {
    WholesaleInquiry createInquiry(WholesaleInquiry inquiry);
    boolean coopQuoteOrAccept(Long inquiryId, BigDecimal quotePrice, boolean accept);
    Map<String, Object> generateContract(Long inquiryId);
    List<Map<String, Object>> getInquiryList(Long userId, String roleType);
}
