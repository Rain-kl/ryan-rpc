package io.ryan.serviceCenter;

import io.ryan.ratelimit.RateLimit;
import io.ryan.ratelimit.impl.NoRateLimit;
import lombok.Setter;


@Setter
public abstract class AbstractServiceCenter implements ServiceCenter {


    /**
     * 设置全局缓存, 共享同一个限流器
     */
    protected RateLimit globalRateLimit = new NoRateLimit();

}
