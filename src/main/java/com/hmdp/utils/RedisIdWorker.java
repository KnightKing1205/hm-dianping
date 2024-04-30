package com.hmdp.utils;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author 65199
 * @ClassName RedisIdWorker
 * @description: TODO
 * @date 2024年04月28日
 * @version: 1.0
 */
@Component
public class RedisIdWorker {
    private static final long BEGIN_TIMESTAMP = 1711929600L;
    private static final int COUNT_BITS = 32;
    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix){
        //获取时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;

        //生成序列号
        //获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        //自增长
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);


        //拼接返回

        return timeStamp << COUNT_BITS | count;
    }

//    public static void main(String[] args) {
//        //获取这个时间的对象
//        LocalDateTime time = LocalDateTime.of(2024, 4, 1, 0, 0, 0);
//        //获取这个时间对象的时间所对应的秒数
//        long second = time.toEpochSecond(ZoneOffset.UTC);
//        System.out.println("second = "+ second);
//    }
}
