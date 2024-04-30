package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-01-04
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {
    @Autowired
    private ISeckillVoucherService iSeckillVoucherService;
    @Autowired
    private RedisIdWorker redisIdWorker;
    @Autowired
    private IVoucherOrderService iVoucherOrderService;
    @Override
    @Transactional//事务，对数据的操作发生错误可以回滚
    public Result seckillVoucher(Long id) {
        //1.查询优惠卷
        SeckillVoucher byId = iSeckillVoucherService.getById(id);
        //2.判断秒杀是否开始
        if (byId == null){
            return Result.fail("秒杀尚未开始");
        }
        if (byId.getBeginTime().isAfter(LocalDateTime.now())){
            return Result.fail("秒杀尚未开始");
        }
        //3.判断秒杀是否已经结束
        if (LocalDateTime.now().isAfter(byId.getEndTime())){
            return Result.fail("秒杀结束");
        }
        //4.判断库存是否充足
        if (byId.getStock()<1){
            return Result.fail("秒杀卷已用完");
        }
        //5.扣减库存
//        byId.setStock(byId.getStock()-1);
//        iSeckillVoucherService.updateById(byId);
        boolean success = iSeckillVoucherService.update()
                .setSql("stock = stock - 1")//set stock = stock - 1
                .eq("voucher_id", id).gt("stock", 0)//where id = ? and stock > ?
                .update();

        if (!success){
            return Result.fail("库存不足");
        }
        //查询订单，看看是否已经下单过
        //6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        Long userId = UserHolder.getUser().getId();
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(id);
        iVoucherOrderService.save(voucherOrder);


        //返回订单id
        return Result.ok(orderId);
    }
}
