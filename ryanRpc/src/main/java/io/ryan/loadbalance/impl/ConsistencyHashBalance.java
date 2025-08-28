package io.ryan.loadbalance.impl;

import io.ryan.common.utils.Hashing;
import io.ryan.loadbalance.LoadBalance;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ConsistencyHashBalance implements LoadBalance<String> {

    // 建议可配置
    static int BASE_VIRTUAL_NUM = 160;
    private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    // 使用不可变快照思路：所有查询只读 ring 引用；重建时整体替换
    private volatile NavigableMap<Long, String> ring = new TreeMap<>();
    private volatile Set<String> realNodes = Collections.emptySet();
    // 用于检测 serviceList 是否变化（排序+join 后做一次指纹）
    private volatile String lastSignature = "";

    public ConsistencyHashBalance() {
    }

    public ConsistencyHashBalance(List<String> addressList) {
        ensureRingBuilt(addressList);
    }

    @Override
    public String select(String hashKey) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("No available server nodes.");
        }
        long h = Hashing.murmurHash64(hashKey);
        Map.Entry<Long, String> e = ring.ceilingEntry(h);
        if (e == null) e = ring.firstEntry();
        return e.getValue();
    }

    @Override
    public String select(List<String> addressList, String hashKey) {
        ensureRingBuilt(addressList);
        return select(hashKey);
    }


    @Override
    public String select() {
        throw new IllegalStateException("ConsistencyHashBalance requires a hash key. Please use balance(addressList, hashKey).");
    }

    @Override
    public String select(List<String> list) {
        throw new IllegalStateException("ConsistencyHashBalance requires a hash key. Please use balance(addressList, hashKey).");
    }

    // 动态加/删节点（可选：外部用全量替换更稳）
    public void addNode(String node) {
        rw.writeLock().lock();
        try {
            Set<String> newReal = new LinkedHashSet<>(realNodes);
            if (newReal.add(node)) {
                NavigableMap<Long, String> newRing = new TreeMap<>(ring);
                addReplicas(newRing, node);
                ring = Collections.unmodifiableNavigableMap(newRing);
                realNodes = Collections.unmodifiableSet(newReal);
                lastSignature = ""; // 强制下次全量校验不误判
            }
        } finally {
            rw.writeLock().unlock();
        }
    }

    public void delNode(String node) {
        rw.writeLock().lock();
        try {
            if (!realNodes.contains(node)) return;
            NavigableMap<Long, String> newRing = new TreeMap<>(ring);
            removeReplicas(newRing, node);
            Set<String> newReal = new LinkedHashSet<>(realNodes);
            newReal.remove(node);
            ring = Collections.unmodifiableNavigableMap(newRing);
            realNodes = Collections.unmodifiableSet(newReal);
            lastSignature = "";
        } finally {
            rw.writeLock().unlock();
        }
    }


    private void ensureRingBuilt(List<String> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) {
            rw.writeLock().lock();
            try {
                ring = new TreeMap<>();
                realNodes = Collections.emptySet();
                lastSignature = "";
            } finally {
                rw.writeLock().unlock();
            }
            return;
        }

        // 计算签名，避免每次都重建
        List<String> copy = new ArrayList<>(serviceList);
        Collections.sort(copy);
        String signature = String.join("|", copy);

        if (signature.equals(lastSignature)) return;

        rw.writeLock().lock();
        try {
            // 二次检查（双重校验，避免并发重复构建）
            if (signature.equals(lastSignature)) return;

            NavigableMap<Long, String> newRing = new TreeMap<>();
            for (String node : copy) {
                addReplicas(newRing, node);
            }
            ring = Collections.unmodifiableNavigableMap(newRing);
            realNodes = Collections.unmodifiableSet(new LinkedHashSet<>(copy));
            lastSignature = signature;
        } finally {
            rw.writeLock().unlock();
        }
    }

    private void addReplicas(NavigableMap<Long, String> map, String node) {
        for (int i = 0; i < ConsistencyHashBalance.BASE_VIRTUAL_NUM; i++) {
            // 以“node#i”作为虚拟点哈希种子，但映射值直接存真实节点名
            long h = Hashing.murmurHash64(node + "#" + i);
            // 简单碰撞处理：线性探测
            while (map.containsKey(h)) h++;
            map.put(h, node);
        }
    }

    private void removeReplicas(NavigableMap<Long, String> map, String node) {
        for (int i = 0; i < ConsistencyHashBalance.BASE_VIRTUAL_NUM; i++) {
            long h = Hashing.murmurHash64(node + "#" + i);
            // 移除当初插入的 key（如果有线性探测，这里可宽松：向上探测少量步数）
            while (map.containsKey(h) && !Objects.equals(map.get(h), node)) {
                h++; // 对齐 addReplicas 的线性探测
            }
            map.remove(h);
        }
    }


}