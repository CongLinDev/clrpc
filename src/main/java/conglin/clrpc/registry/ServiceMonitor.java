package conglin.clrpc.registry;

import java.util.Collection;
import java.util.function.BiConsumer;

import conglin.clrpc.common.Pair;

public interface ServiceMonitor {

    /**
     * 列出所有服务
     * 
     * @return
     */
    Collection<String> listServices();

    /**
     * 监视所有服务
     * 
     * @param handleProvider 回调
     * @param handleConsumer 回调
     */
    void monitor(BiConsumer<String, Collection<Pair<String, String>>> handleProvider,
            BiConsumer<String, Collection<Pair<String, String>>> handleConsumer);

    /**
     * 监视具体的服务
     * 
     * @param serviceName    服务名
     * @param handleProvider 回调
     * @param handleConsumer 回调
     */
    void monitor(String serviceName, BiConsumer<String, Collection<Pair<String, String>>> handleProvider,
            BiConsumer<String, Collection<Pair<String, String>>> handleConsumer);

}