package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
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
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IShopTypeService typeService;
    @Override
    public List<ShopType> queryList() {
        String json = stringRedisTemplate.opsForValue().get("type");
        if (!JSONUtil.isNull(json)) {
            List<ShopType> typeList = JSONUtil.toList(json,ShopType.class);
            log.debug("redis");
            return typeList;
        }
        List<ShopType> typeList = typeService.query().orderByAsc("sort").list();
        if (typeList.isEmpty()){
            return null;
        }
        stringRedisTemplate.opsForValue().set("type",JSONUtil.toJsonStr(typeList),30, TimeUnit.MINUTES);

        return typeList;
    }
}
