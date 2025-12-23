package com.example.financedashboard.controller;

import com.example.financedashboard.dto.UserFavoriteDTO;
import com.example.financedashboard.service.UserFavoriteService;
import com.example.financedashboard.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    @PostMapping("/add")
    public Result<Boolean> addFavorite(@RequestParam(name = "user_id") Long userId,
                                       @RequestParam(name = "stock_code") String stockCode,
                                       @RequestParam(required = false) String remark) {
        boolean success = userFavoriteService.addFavoriteByCode(userId, stockCode, remark);
        return success ? Result.success(true) : Result.error(400, "股票不存在或已在自选列表中");
    }

    @DeleteMapping("/remove")
    public Result<Boolean> removeFavorite(@RequestParam(name = "user_id") Long userId,
                                          @RequestParam(name = "stock_code") String stockCode) {
        boolean success = userFavoriteService.removeFavoriteByCode(userId, stockCode);
        return success ? Result.success(true) : Result.error(404, "自选股不存在");
    }

    @GetMapping("/list")
    public Result<List<UserFavoriteDTO>> getUserFavorites(@RequestParam(name = "user_id") Long userId) {
        List<UserFavoriteDTO> favorites = userFavoriteService.getUserFavoritesWithDetails(userId);
        return Result.success(favorites);
    }

    @GetMapping("/check")
    public Result<Boolean> isFavorite(@RequestParam(name = "user_id") Long userId,
                                      @RequestParam(name = "stock_code") String stockCode) {
        Long stockId = userFavoriteService.getStockIdByCode(stockCode);
        if (stockId == null) {
            return Result.error(404, "股票不存在");
        }
        boolean isFavorite = userFavoriteService.isFavorite(userId, stockId);
        return Result.success(isFavorite);
    }
}
