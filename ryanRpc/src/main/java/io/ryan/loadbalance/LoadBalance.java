package io.ryan.loadbalance;

import java.util.List;

public interface LoadBalance<T> {

    T select(List<T> urls);


}
