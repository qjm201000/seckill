package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.Product;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OrderMongoService {
    @Autowired
    private MongoTemplate mongoTemplate;

    //下单 : 副本集mongodb才能支持事物。意思就是至少两个mongo服务。具体操作查看：https://blog.csdn.net/quanmaoluo5461/article/details/84880850
    @Transactional
    public boolean insert(){
        String productId = "5c23a6b4be592e584c8d7c47";//产品id
        int buyCount = 1;//当前用户下单数量

        if(updateProductAmount(productId,buyCount)){
//            if(true){
//                throw new RuntimeException();
//            }
            Order order = new Order();
            order.setStatus(1);
            return mongoTemplate.insert(order).getId() != null;
        }
        return false;
    }

    //秒杀服务，修改库存
    private boolean updateProductAmount(String productId,int buyCount) {
        Criteria criteria = Criteria.where("id").is(new ObjectId(productId))
                .and("stock").gte(buyCount);
        Query query = new Query(criteria);
        Product product = mongoTemplate.findOne(query,Product.class);
        if (product == null) {
            return false;
        }

        Update update = new Update();
        update.inc("stock", -buyCount);
        UpdateResult updateResult= mongoTemplate.updateFirst(query, update, Product.class);
        if(updateResult.getModifiedCount() > 0){
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
