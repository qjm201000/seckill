package com.example.demo.service;

import com.example.demo.mapper.OrderInfoMapper;
import com.example.demo.mapper.ProductInfoMapper;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OrderMysqlService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private ProductInfoMapper productInfoMapper;

    //下单
    public boolean insert(){
        Long productId = 22l;//产品id
        int buyCount = 1;//当前用户下单数量

        if(updateProductAmount(productId,buyCount)){
//            if(true){
//                throw new RuntimeException();
//            }
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setStatus(1);
            orderInfo.setCreateTime(new Date());
            return orderInfoMapper.insertSelective(orderInfo) > 0;
        }
        return false;
    }

    //秒杀服务，修改库存
    private boolean updateProductAmount(Long productId,int buyCount) {
        ProductInfo productInfo = productInfoMapper.selectByPrimaryKeyByBuyCount(productId,buyCount);
        if (productInfo == null) {
            return false;
        }

        if (productInfoMapper.updateByStock(productId, buyCount) > 0) {
            return true;
        }

        //如果更新失败，当前线程休眠，错峰执行(同时执行的话，还是只有一个人抢占到资源，别的都失败，所以错峰执行)
        waitForLock();
        return updateProductAmount(productId, buyCount);
    }

    private void waitForLock(){
        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(10) + 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
