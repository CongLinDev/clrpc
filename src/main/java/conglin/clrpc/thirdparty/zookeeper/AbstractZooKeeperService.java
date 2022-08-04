package conglin.clrpc.thirdparty.zookeeper;

import java.util.Properties;

import conglin.clrpc.lifecycle.Destroyable;

abstract public class AbstractZooKeeperService implements Destroyable {
    private static final String SEPARATOR = "/";
    protected final String rootPath; // zookeeper根地址
    protected final ZooKeeperInstance keeperInstance;

    /**
     * 从配置文件获取连接信息
     * 
     * @param prefix
     * @param properties
     * @return
     */
    public static ZooKeeperConnectionInfo getConnectionInfo(String prefix, Properties properties) {
        ZooKeeperConnectionInfo connectionInfo = new ZooKeeperConnectionInfo(
                properties.getProperty(prefix + "zookeeper.connection"));
        connectionInfo.setSessionTimeoutValue(properties.getProperty(prefix + "zookeeper.session-timeout"));
        connectionInfo.setPath(properties.getProperty(prefix + "zookeeper.path"));
        return connectionInfo;
    }

    public AbstractZooKeeperService(ZooKeeperConnectionInfo connectionInfo) {
        keeperInstance = ZooKeeperInstance.connect(connectionInfo);
        rootPath = connectionInfo.getPath();
    }

    public AbstractZooKeeperService(ZooKeeperConnectionInfo connectionInfo, String serviceNode) {
        keeperInstance = ZooKeeperInstance.connect(connectionInfo);
        rootPath = connectionInfo.getPath() + SEPARATOR + serviceNode;
    }

    protected String buildPath(String... subPaths) {
        if (subPaths == null || subPaths.length == 0)
            return rootPath;
        StringBuilder stringBuilder = new StringBuilder(rootPath);
        for (String path : subPaths) {
            stringBuilder.append(SEPARATOR).append(path);
        }
        return stringBuilder.toString();
    }

    @Override
    public void destroy() {
        keeperInstance.release();
    }
}