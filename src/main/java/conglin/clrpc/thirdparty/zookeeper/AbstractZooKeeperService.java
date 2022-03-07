package conglin.clrpc.thirdparty.zookeeper;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperInstance;

import conglin.clrpc.common.object.UrlScheme;

abstract public class AbstractZooKeeperService implements Destroyable {
    private static final String SEPARATOR = "/";
    protected final String rootPath; // zookeeper根地址
    protected final ZooKeeperInstance keeperInstance;

    private boolean destroy = false;

    public AbstractZooKeeperService(UrlScheme url) {
        keeperInstance = ZooKeeperInstance.connect(url);
        rootPath = url.getPath();
    }

    public AbstractZooKeeperService(UrlScheme url, String serviceNode) {
        keeperInstance = ZooKeeperInstance.connect(url);
        rootPath = url.getPath() + SEPARATOR + serviceNode;
    }

    protected String buildPath(String... subPaths) {
        if (subPaths == null || subPaths.length == 0) return rootPath;
        StringBuilder stringBuilder = new StringBuilder(rootPath);
        for (String path : subPaths) {
            stringBuilder.append(SEPARATOR).append(path);
        }
        return stringBuilder.toString();
    }

    @Override
    public void destroy() throws DestroyFailedException {
        keeperInstance.destroy();
        destroy = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroy;
    }
}