package io.ryan.serviceCenter.impl.zooKeeperImpl;

import io.ryan.serviceCenter.cache.ServiceCache;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;


public class ZKDataWatcher {

    private final CuratorFramework client;

    final ServiceCache cache;

    public ZKDataWatcher(CuratorFramework client, ServiceCache cache) {
        this.client = client;
        this.cache = cache;
    }

    /**
     * 监听当前节点和子节点的 更新，创建，删除
     *
     * @param path 地址
     */
    public void watchToUpdate(String path) throws InterruptedException {
        CuratorCache curatorCache = CuratorCache.build(client, "/");

        // 事件类型（枚举, 节点更新前的状态、数据, 节点更新后的状态、数据
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldChildData, ChildData childData) {
                // 创建节点时：节点刚被创建，不存在 更新前节点 ，所以第二个参数为 null
                // 删除节点时：节点被删除，不存在 更新后节点 ，所以第三个参数为 null
                // 节点创建时没有赋予值 create /curator/app1 只创建节点，在这种情况下，更新前节点的 data 为 null，获取不到更新前节点的数据
                switch (type.name()) {
                    case "NODE_CREATED": // 监听器第一次执行时节点存在也会触发次事件
                        String[] pathList = parsePath(childData);
                        if (pathList.length <= 2) break;
                        else {
                            String serviceName = pathList[1];
                            String address = pathList[2];
                            //将新注册的服务加入到本地缓存中
                            cache.addServiceToCache(serviceName, address);
                        }
                        break;
                    case "NODE_CHANGED": // 节点更新
                        if (oldChildData.getData() != null) {
                            System.out.println("修改前的数据: " + new String(oldChildData.getData()));
                        } else {
                            System.out.println("节点第一次赋值!");
                        }
                        String[] oldPathList = parsePath(oldChildData);
                        String[] newPathList = parsePath(childData);
                        cache.replaceServiceAddress(oldPathList[1], oldPathList[2], newPathList[2]);
                        System.out.println("修改后的数据: " + new String(childData.getData()));
                        break;
                    case "NODE_DELETED": // 节点删除
                        String[] pathList_d = parsePath(oldChildData);
                        if (pathList_d.length <= 2) break;
                        else {
                            String serviceName = pathList_d[1];
                            String address = pathList_d[2];
                            //将新注册的服务加入到本地缓存中
                            cache.delete(serviceName, address);
                        }
                        break;
                    default:
                        break;
            }
        } });
        //开启监听
        curatorCache.start();
    }

    //解析节点对应地址
    public String[] parsePath(ChildData childData) {
        //获取更新的节点的路径
        String path = childData.getPath();
        //按照格式 ，读取
        return path.split("/");
    }
}