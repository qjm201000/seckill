package com.example.demo;

import com.example.demo.service.OrderMongoService;
import com.example.demo.service.OrderMysqlService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MainTest {
    @Autowired
    private OrderMysqlService orderMysqlService;
    @Autowired
    private OrderMongoService orderMongoService;

    public static AtomicInteger successThread = new AtomicInteger();

    @Test
    public void createOrder_mysql(){
        //mysql方式
        //秒杀服务，下单,模拟1000人同时下单
        insert(orderMysqlService);
    }

    @Test
    public void createOrder_mongo(){
        //mongo方式
        //秒杀服务，下单模拟1000人同时下单
        insert(orderMongoService);
    }

    public void insert(Object objectService) {
        int test_count = 1000;//1000人同时下单

        CyclicBarrier cyclicBarrier = new CyclicBarrier(test_count);
        for(int i = 0;i < test_count;i++){
            MainTest.TestThread test = new MainTest.TestThread(cyclicBarrier,objectService);
            new Thread(test).start();
        }

        try {
            //主线程休眠3秒
            TimeUnit.SECONDS.sleep(3);
            System.out.println("成功进入线程数："+successThread);
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class TestThread implements Runnable{
        private CyclicBarrier cyclicBarrier;
        private Object objectService;
        TestThread(CyclicBarrier cyclicBarrier,Object objectService){
            this.cyclicBarrier = cyclicBarrier;
            this.objectService = objectService;
        }

        @Override
        public void run() {
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }

            try{
                //下单
                boolean result = false;
                if(objectService instanceof OrderMongoService){
                    OrderMongoService orderMongoService = (OrderMongoService)objectService;
                    result = orderMongoService.insert();
                }else if(objectService instanceof  OrderMysqlService){
                    OrderMysqlService orderMysqlService = (OrderMysqlService)objectService;
                    result = orderMysqlService.insert();
                }

                if(result){
                    successThread.addAndGet(1);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
