package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //保存菜品基本信息到菜品表dish
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        //获取新插入的菜品id
        Long dishId = dish.getId();

        //保存菜品口味数据到菜品口味表dish_flavor
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            //给每个菜品口味设置菜品id
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //由分页插件（如PageHelper）自动拦截该SQL并添加分页逻辑
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        //vo用于前端页面展示，dto用于前后端数据库交互，pojo是对应的数据库表的字段
        //因为根据接口文件前端要求展示categoryName分类名称，但是表中只有分类id，所以设计一个vo
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(
                page.getTotal(),
                page.getResult()
        );
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断是否有起售中的菜品
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus().equals(StatusConstant.ENABLE)){
                //起售中的菜品不能删除
                throw new RuntimeException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断该菜品是否被套餐关联
        List<Long> setmealIds =setmealDishMapper.getSetmealIdsByDishIds(ids);

        /**
         List<String> list = new ArrayList<>();  // 初始化但未添加元素
         System.out.println(list == null);      // 输出 false（非空）
         System.out.println(list.isEmpty());    // 输出 true（无数据）
         System.out.println(list.size());       // 输出 0（无数据）
         */
        if(setmealIds != null && setmealIds.size() > 0){
            //关联了套餐，不能删除
            throw new RuntimeException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
//        //删除菜品表中的数据
//        for(Long id : ids){
//            dishMapper.deleteById(id);
//            //根据菜品id删除菜品口味表中的数据
//            dishFlavorMapper.deleteByDishId(id);
//        }
        //删除菜品表中的数据
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品基本信息
        Dish dish = dishMapper.getById(id);

        //根据菜品id查询口味信息
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //将查询到的数据封装到VO中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //口味数据，先删除后插入
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
