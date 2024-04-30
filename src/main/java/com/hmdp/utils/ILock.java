package com.hmdp.utils;

/**
 * @author 65199
 */
public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec
     * @return
     */
    boolean tryLock (Long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
