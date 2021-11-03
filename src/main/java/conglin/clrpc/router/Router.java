package conglin.clrpc.router;

import conglin.clrpc.common.object.Pair;

public interface Router<K, V> {
    /**
     * choose
     *
     * @param condition
     * @return
     * @throws NoAvailableServiceInstancesException
     */
    Pair<K, V> choose(RouterCondition<K> condition) throws NoAvailableServiceInstancesException;

    /**
     * 刷新
     */
    void refresh();
}
