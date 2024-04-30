package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryById(Long id) {
        //从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get("shop" + id);
        //判断是否存在
        if (StrUtil.isNotBlank(json)) {
            //存在直接返回
            //反序列化,即将json转为对象
            log.debug("jinlaile");
            Shop shop = JSONUtil.toBean(json, Shop.class);
            return Result.ok(shop);
        }
        //命中的是否是空值
        if ("".equals(json)) {
            log.debug("123");
            return Result.fail("店铺不存在");

        }
        //不存在根据id查询数据库
        Shop shop = getById(id);

        //不存在返回错误
        if (shop == null){
            //将空值写入redis
            stringRedisTemplate.opsForValue().set("shop"+id,"",10, TimeUnit.MINUTES);
            //
            return Result.fail("店铺不存在");
        }
        //存在 写入redis
        stringRedisTemplate.opsForValue().set("shop"+id,JSONUtil.toJsonStr(shop),30, TimeUnit.MINUTES);

        return Result.ok(shop);
    }

    @Override
    //事务的注释，主要是为了回滚数据库的更新
    @Transactional
    public Result updateBy(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("商铺id不能为空");
        }
        //更新数据库
        updateById(shop);
        //删除缓存
        stringRedisTemplate.delete("shop"+ id);
        return Result.ok();
    }
}
