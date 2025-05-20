package com.sky.task;

import com.google.j2objc.annotations.AutoreleasePool;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        LocalDateTime  time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);

        if(list != null && list.size() > 0){
            for (Orders orders : list) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }
    /**
     * 处理待派送订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理待派送订单:{}", LocalDateTime.now());

        LocalDateTime  time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.CONFIRMED, time);

        if(list != null && list.size() > 0){
            for (Orders orders : list) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

}
