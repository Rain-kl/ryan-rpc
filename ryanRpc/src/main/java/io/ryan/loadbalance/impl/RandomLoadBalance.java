package io.ryan.loadbalance.impl;

import io.ryan.loadbalance.LoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance<T> implements LoadBalance<T> {

    Random random;
    List<T> addressList;

    public RandomLoadBalance() {
    }


    public RandomLoadBalance(List<T> addressList) {
        random = new Random();
        this.addressList = addressList;
    }

    @Override
    public T select() {
        try {
            int nextInt = random.nextInt(addressList.size());
            return addressList.get(nextInt);
        } catch (Exception e) {
            throw new RuntimeException("LoadBalance Error: list is empty");
        }
    }

    @Override
    public T select(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T select(List<T> list) {
        this.addressList = list;
        return select();
    }

    @Override
    public T select(List<T> list, String key) {
        throw new UnsupportedOperationException();
    }
}
