package com.example.financedashboard.service;

import com.example.financedashboard.dto.UserFavoriteDTO;
import com.example.financedashboard.entity.UserFavorite;

import java.util.List;

public interface UserFavoriteService {
    boolean addFavorite(Long userId, Long stockId, String remark);
    boolean addFavoriteByCode(Long userId, String stockCode, String remark);
    boolean removeFavorite(Long userId, Long stockId);
    boolean removeFavoriteByCode(Long userId, String stockCode);
    List<UserFavorite> getUserFavorites(Long userId);
    List<UserFavoriteDTO> getUserFavoritesWithDetails(Long userId);
    boolean isFavorite(Long userId, Long stockId);
    Long getStockIdByCode(String stockCode);
}
