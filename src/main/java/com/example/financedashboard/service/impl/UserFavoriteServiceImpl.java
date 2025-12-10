package com.example.financedashboard.service.impl;

import com.example.financedashboard.entity.UserFavorite;
import com.example.financedashboard.mapper.StockInfoMapper;
import com.example.financedashboard.mapper.UserFavoriteMapper;
import com.example.financedashboard.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Override
    public boolean addFavorite(Long userId, Long stockId, String remark) {
        if (isFavorite(userId, stockId)) {
            return false;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setStockId(stockId);
        favorite.setRemark(remark);
        return userFavoriteMapper.insert(favorite) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Long stockId) {
        return userFavoriteMapper.deleteByUserIdAndStockId(userId, stockId) > 0;
    }

    @Override
    public List<UserFavorite> getUserFavorites(Long userId) {
        return userFavoriteMapper.findByUserId(userId);
    }

    @Override
    public boolean isFavorite(Long userId, Long stockId) {
        return userFavoriteMapper.findByUserIdAndStockId(userId, stockId) != null;
    }

    @Override
    public boolean addFavoriteByCode(Long userId, String stockCode, String remark) {
        Long stockId = stockInfoMapper.findIdByCode(stockCode);
        if (stockId == null) {
            return false;
        }
        return addFavorite(userId, stockId, remark);
    }

    @Override
    public boolean removeFavoriteByCode(Long userId, String stockCode) {
        Long stockId = stockInfoMapper.findIdByCode(stockCode);
        if (stockId == null) {
            return false;
        }
        return removeFavorite(userId, stockId);
    }

    @Override
    public Long getStockIdByCode(String stockCode) {
        return stockInfoMapper.findIdByCode(stockCode);
    }
}
