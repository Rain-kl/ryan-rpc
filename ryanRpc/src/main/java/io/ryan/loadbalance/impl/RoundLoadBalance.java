package io.ryan.loadbalance.impl;

import io.ryan.loadbalance.LoadBalance;

import java.util.List;

public class RoundLoadBalance<T> implements LoadBalance<T> {

    private int choose = -1;

    @Override
    public T select(List<T> addressList) {
        choose++;
        choose = choose % addressList.size();
        System.out.println("负载均衡选择了" + choose + "服务器");
        return addressList.get(choose);
    }

    @Override
    public T select(List<T> list, String key) {
        throw new UnsupportedOperationException();
    }
}
