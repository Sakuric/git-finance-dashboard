package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFavoriteMapper {
    int insert(UserFavorite userFavorite);
    int deleteById(@Param("id") Long id);
    int deleteByUserIdAndStockId(@Param("userId") Long userId, @Param("stockId") Long stockId);
    List<UserFavorite> findByUserId(@Param("userId") Long userId);
    UserFavorite findByUserIdAndStockId(@Param("userId") Long userId, @Param("stockId") Long stockId);
}
