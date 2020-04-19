package conglin.clrpc.zookeeper;

import org.apache.zookeeper.ZooKeeper;

import conglin.clrpc.common.Url;
import conglin.clrpc.zookeeper.util.ZooKeeperUtils;

abstract public class AbstractZooKeeperService {

    protected final String rootPath; // zookeeper根地址
    protected final ZooKeeper keeper;

    public AbstractZooKeeperService(Url url) {
        keeper = ZooKeeperUtils.connectZooKeeper(url);
        rootPath = url.getPath();
    }

    public AbstractZooKeeperService(Url url, String serviceNode) {
        keeper = ZooKeeperUtils.connectZooKeeper(url);
        rootPath = url.getPath() + "/" + serviceNode;
    }
}