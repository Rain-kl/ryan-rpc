package io.ryan.loadbalance;

import io.ryan.common.URL;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance<URL> {

    @Override
    public URL select(List<URL> urls) {
        Random random = new Random();
        int nextInt = random.nextInt(urls.size());
        return urls.get(nextInt);
    }
}
