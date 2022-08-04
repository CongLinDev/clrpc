package conglin.clrpc.thirdparty.zookeeper;

import conglin.clrpc.common.CommonState;
import conglin.clrpc.common.StateRecord;
import conglin.clrpc.lifecycle.Available;
import conglin.clrpc.lifecycle.Destroyable;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;

import org.apache.zookeeper.ZooKeeper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ZooKeeperInstance implements Destroyable, Available {

    private final StateRecord<CommonState> stateRecord;
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
    private static ZooKeeperInstance connect(final String address, final int sessionTimeout) {
        String cacheKey = String.format(ZOOKEEPER_CONNECTION_POOL_KEY_FORMAT, address, sessionTimeout);
        ZooKeeperInstance instance = ZOOKEEPER_CONNECTION_POOL.get(cacheKey);
        if (instance == null) {
            ZooKeeper keeper = ZooKeeperUtils.connectNewZooKeeper(address, sessionTimeout);
            instance = new ZooKeeperInstance(cacheKey, keeper, 0);
            ZOOKEEPER_CONNECTION_POOL.put(cacheKey, instance);
        }
        instance.acquire();
        return instance;
    }

    /**
     * 连接
     *
     * @param connectionInfo
     * @return
     */
    public static ZooKeeperInstance connect(ZooKeeperConnectionInfo connectionInfo) {
        return connect(connectionInfo.getConnectString(), connectionInfo.getSessionTimeout());
    }

    /**
     * 移除缓存
     *
     * @param cacheKey
     * @return
     */
    protected static ZooKeeperInstance removeCache(String cacheKey) {
        return ZOOKEEPER_CONNECTION_POOL.remove(cacheKey);
    }

    protected ZooKeeperInstance(String cacheKey, ZooKeeper keeper) {
        this(cacheKey, keeper, 0);
    }

    protected ZooKeeperInstance(String cacheKey, ZooKeeper keeper, int count) {
        this.cacheKey = cacheKey;
        this.keeper = keeper;
        this.count = new AtomicInteger(count);
        this.stateRecord = new StateRecord<>(CommonState.AVAILABLE);
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
    protected String getCacheKey() {
        return cacheKey;
    }

    public int acquire() {
        stateRecord.except(CommonState.AVAILABLE);
        return count.incrementAndGet();
    }

    public int release() {
        stateRecord.except(CommonState.AVAILABLE);
        int countValue = count.decrementAndGet();
        if (countValue <= 0) {
            destroy();
        }
        return countValue;
    }

    public int count() {
        return count.get();
    }

    @Override
    public void destroy() {
        if (stateRecord.compareAndSetState(CommonState.AVAILABLE, CommonState.DESTORYING)) {
            ZooKeeperInstance.removeCache(getCacheKey());
            ZooKeeperUtils.disconnectZooKeeper(instance());
            stateRecord.setState(CommonState.UNAVAILABLE);
        }
    }

    @Override
    public boolean isAvailable() {
        return stateRecord.isState(CommonState.AVAILABLE);
    }
}
