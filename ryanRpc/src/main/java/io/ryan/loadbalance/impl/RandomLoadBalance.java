package io.ryan.loadbalance.impl;

import io.ryan.loadbalance.LoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance<T> implements LoadBalance<T> {

    @Override
    public T select(List<T> list) {
        try {
            Random random = new Random();
            int nextInt = random.nextInt(list.size());
            return list.get(nextInt);
        } catch (Exception e) {
            throw new RuntimeException("LoadBalance Error: list is empty");
        }

    }

    @Override
    public T select(List<T> list, String key) {
        throw new UnsupportedOperationException();
    }
}
