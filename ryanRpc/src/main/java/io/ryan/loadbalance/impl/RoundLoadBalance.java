package io.ryan.loadbalance.impl;

import io.ryan.loadbalance.LoadBalance;

import java.util.List;

public class RoundLoadBalance<T> implements LoadBalance<T> {

    private int choose = -1;

    List<T> addressList;

    public RoundLoadBalance() {
    }

    public RoundLoadBalance(List<T> addressList) {
        this.addressList = addressList;
    }


    @Override
    public T select() {
        choose++;
        choose = choose % addressList.size();
//        System.out.println("负载均衡选择了第" + choose + "服务器");
        return addressList.get(choose);
    }


    @Override
    public T select(List<T> addressList) {
        this.addressList = addressList;
        return select();
    }

    @Override
    public T select(String key) {
        throw new UnsupportedOperationException();

    }

    @Override
    public T select(List<T> list, String key) {
        throw new UnsupportedOperationException();
    }
}
