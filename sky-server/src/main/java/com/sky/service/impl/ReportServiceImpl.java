package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper  orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //集合用于存放时间
        List<LocalDate> timeList = new ArrayList<>();
        timeList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            timeList.add(begin);
        }
        //集合用于存放当日营业额
        List<Double> amountList = new ArrayList<>();
        for (LocalDate time : timeList) {
            //还需要获得时分秒
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            amountList.add(turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(timeList,","))
                .turnoverList(StringUtils.join(amountList,","))
                .build();

    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //集合用于存放时间
        List<LocalDate> timeList = new ArrayList<>();
        timeList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            timeList.add(begin);
        }

        //集合用于存放新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //集合用于存放总用户数量
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate time : timeList) {
            //还需要获得时分秒
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);
            //先查总人数
            Map map = new HashMap();
            map.put("end",endTime);
            Integer totalUser = userMapper.countByMap(map);
            //再查新增人数
            map.put("begin",beginTime);
            Integer totalNewUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
            newUserList.add(totalNewUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(timeList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }


    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //集合用于存放时间
        List<LocalDate> timeList = new ArrayList<>();
        timeList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            timeList.add(begin);
        }
        //集合用于存每日的订单数和有效订单数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate time : timeList) {
            //还需要获得时分秒
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);
            //1_订单数量,2_有效订单数量
            Map map_1 = new HashMap();
            Map map_2 = new HashMap();
            map_1.put("begin", beginTime);
            map_1.put("end", endTime);
            map_2.put("begin", beginTime);
            map_2.put("end", endTime);
            map_2.put("status", Orders.COMPLETED);
            Integer orderCount = orderMapper.countByMap(map_1);
            Integer validOrderCount = orderMapper.countByMap(map_2);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //计算总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算有效订单数
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = 0.0;

        if (totalOrderCount != 0) {
            orderCompletionRate = totalValidOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(timeList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


}
