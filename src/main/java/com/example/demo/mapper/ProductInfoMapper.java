package com.example.demo.mapper;

import com.example.demo.model.ProductInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductInfoMapper {
    ProductInfo selectByPrimaryKeyByBuyCount(@Param("productId") Long productId,@Param("buyCount")int buyCount);
    int updateByStock(@Param("productId") Long productId,@Param("buyCount")int buyCount);
}