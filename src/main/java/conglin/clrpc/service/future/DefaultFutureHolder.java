package conglin.clrpc.service.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFutureHolder implements FutureHolder<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFutureHolder.class);

    private final Map<Long, RpcFuture> rpcFutures;
    /**
     * 对于每个 Request 请求，都会有一个 RpcFuture 等待一个 Response 响应 这些未到达客户端的
     * Response 响应 换言之即为 RpcFuture 被保存在 ConsumerServiceHandler 中的一个 list 中
     * 以下代码用于 RpcFuture 的管理和维护
     */

    public DefaultFutureHolder() {
        rpcFutures = new ConcurrentHashMap<>();
    }

    @Override
    public void putFuture(Long key, RpcFuture rpcFuture) {
        rpcFutures.put(key, rpcFuture);
    }

    @Override
    public RpcFuture getFuture(Long key) {
        return rpcFutures.get(key);
    }

    @Override
    public RpcFuture removeFuture(Long key) {
        return rpcFutures.remove(key);
    }

    @Override
    public Iterator<RpcFuture> iterator() {
        return rpcFutures.values().iterator();
    }

    @Override
    public void waitForUncompletedFuture() {
        while (!rpcFutures.isEmpty()) {
            try {
                LOGGER.info("Waiting uncompleted futures for 500 ms.");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}