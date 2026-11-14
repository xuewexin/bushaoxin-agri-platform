package com.agri.platform.controller;

import com.agri.platform.common.Result;
import com.agri.platform.entity.LiveRoom;
import com.agri.platform.service.LiveRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/live")
public class LiveController {

    @Autowired
    private LiveRoomService liveRoomService;

    @GetMapping("/list")
    public Result<List<LiveRoom>> getLiveList() {
        return Result.success(liveRoomService.list());
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getLiveDetail(@PathVariable Long id) {
        try {
            return Result.success(liveRoomService.getLiveDetail(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/switch-product")
    public Result<Boolean> switchProduct(@RequestBody Map<String, Object> body) {
        Long liveId = Long.valueOf(body.get("liveId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        boolean success = liveRoomService.switchProduct(liveId, productId);
        return Result.success("已切换主推讲解商品", success);
    }

    @PostMapping("/status")
    public Result<Boolean> changeStatus(@RequestBody Map<String, Object> body) {
        Long liveId = Long.valueOf(body.get("liveId").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        boolean success = liveRoomService.changeLiveStatus(liveId, status);
        return Result.success(success);
    }

    @GetMapping("/report/{id}")
    public Result<Map<String, Object>> getLiveReport(@PathVariable Long id) {
        return Result.success(liveRoomService.getLiveReport(id));
    }
}
