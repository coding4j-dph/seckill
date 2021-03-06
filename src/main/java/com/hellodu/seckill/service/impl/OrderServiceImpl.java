package com.hellodu.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hellodu.seckill.entity.Order;
import com.hellodu.seckill.entity.SeckillGoods;
import com.hellodu.seckill.entity.SeckillOrder;
import com.hellodu.seckill.entity.User;
import com.hellodu.seckill.entity.vo.GoodsVo;
import com.hellodu.seckill.entity.vo.OrderDetailVo;
import com.hellodu.seckill.mapper.OrderMapper;
import com.hellodu.seckill.service.GoodsService;
import com.hellodu.seckill.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hellodu.seckill.service.SeckillGoodsService;
import com.hellodu.seckill.service.SeckillOrderService;
import com.hellodu.seckill.utils.ResultCode;
import com.hellodu.seckill.utils.exceptionhandler.MyExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dupeiheng
 * @since 2021-12-16
 */
@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 创建订单
     * @param user
     * @param goodsVo
     * @return
     */
    @Override
    public Order createOrder(User user, GoodsVo goodsVo) {
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        seckillGoodsService.updateById(seckillGoods);

        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId("12222111");
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateTime(new Date());

        // 生成订单
        orderService.save(order);

        // 生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(goodsVo.getId());

        seckillOrderService.save(seckillOrder);
        return order;
    }

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    @Override
    public OrderDetailVo getOrderDetail(String orderId) {
        if(StringUtils.isEmpty(orderId)) throw new MyExceptionHandler(ResultCode.OrderNotExist, "订单不存在");
        Order order = orderService.getById(orderId);
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoodsVo(goodsVo);
        return vo;
    }
}
