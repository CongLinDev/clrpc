package conglin.clrpc.thirdparty.zookeeper;

import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperInstance;

import conglin.clrpc.common.object.UrlScheme;

abstract public class AbstractZooKeeperService {

    protected final String rootPath; // zookeeper根地址
    protected final ZooKeeperInstance keeperInstance;

    public AbstractZooKeeperService(UrlScheme url) {
        keeperInstance = ZooKeeperInstance.connect(url);
        rootPath = url.getPath();
    }

    public AbstractZooKeeperService(UrlScheme url, String serviceNode) {
        keeperInstance = ZooKeeperInstance.connect(url);
        rootPath = url.getPath() + "/" + serviceNode;
    }
}