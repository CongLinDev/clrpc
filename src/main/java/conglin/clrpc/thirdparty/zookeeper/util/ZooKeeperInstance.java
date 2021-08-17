package conglin.clrpc.thirdparty.zookeeper.util;

import conglin.clrpc.common.Available;
import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.object.UrlScheme;
import org.apache.zookeeper.ZooKeeper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ZooKeeperInstance implements Destroyable, Available {

    private final ZooKeeper keeper;

    private final AtomicInteger count;

    private final String cacheKey;

    protected static String ZOOKEEPER_CONNECTION_POOL_KEY_FORMAT = "%s-%d";

    protected static Map<String, ZooKeeperInstance> ZOOKEEPER_CONNECTION_POOL = new HashMap<>();

    /**
     * 连接
     *
     * @param address
     * @param sessionTimeout
     * @return
     */
    public static ZooKeeperInstance connect(final String address, final int sessionTimeout) {
        String cacheKey = String.format(ZOOKEEPER_CONNECTION_POOL_KEY_FORMAT, address, sessionTimeout);
        ZooKeeperInstance instance = ZOOKEEPER_CONNECTION_POOL.get(cacheKey);
        if(instance == null) {
            ZooKeeper keeper = ZooKeeperUtils.connectNewZooKeeper(address, sessionTimeout);
            instance = new ZooKeeperInstance(cacheKey, keeper, 1);
            ZOOKEEPER_CONNECTION_POOL.put(cacheKey, instance);
        }

        return instance;
    }

    /**
     * 连接
     *
     * @param scheme
     * @return
     */
    public static ZooKeeperInstance connect(UrlScheme scheme) {
        return connect(scheme.getAddress(), Integer.parseInt(scheme.getParameterOrDefault("session-timeout", String.valueOf(ZooKeeperUtils.DEFAULT_SESSION_TIMEOUT))));
    }

    /**
     * 移除缓存
     *
     * @param cacheKey
     * @return
     */
    public static ZooKeeperInstance removeCache(String cacheKey) {
        return ZOOKEEPER_CONNECTION_POOL.remove(cacheKey);
    }

    protected ZooKeeperInstance(String cacheKey, ZooKeeper keeper) {
        this(cacheKey, keeper, 0);
    }

    protected ZooKeeperInstance(String cacheKey, ZooKeeper keeper, int count) {
        this.cacheKey = cacheKey;
        this.keeper = keeper;
        this.count = new AtomicInteger(count);
    }

    /**
     * instance
     *
     * @return
     */
    public ZooKeeper instance() {
        return keeper;
    }

    /**
     * cache key
     *
     * @return
     */
    public String getCacheKey() {
        return cacheKey;
    }


    public int acquire() {
        check();
        return count.incrementAndGet();
    }

    public int release() {
        check();
        return count.decrementAndGet();
    }

    public int releaseAndDestroy() throws DestroyFailedException {
        int c = release();
        if (c <= 0) {
            destroy();
        }
        return c;
    }

    @Override
    public boolean isDestroyed() {
        return Destroyable.super.isDestroyed();
    }

    @Override
    public void destroy() throws DestroyFailedException {
        ZooKeeperInstance.removeCache(getCacheKey());
        ZooKeeperUtils.disconnectZooKeeper(instance());
    }

    protected void check() {
        if(isDestroyed()) {
            throw new UnsupportedOperationException("instance has been destroyed");
        }
    }
}
