package conglin.clrpc.transport.component;

import conglin.clrpc.transport.message.BasicRequest;

/**
 * 服务提供者选择适配器
 *
 * 用于对负载均衡器的随机选择算法补充
 */
@FunctionalInterface
public interface ProviderChooserAdapter {
    /**
     * 自定义策略
     * 
     * @param request
     * @return
     */
    int apply(BasicRequest request);

    /**
     * Hash 算法
     * 
     * @param object
     * @return
     */
    default int hash(Object object) {
        return System.identityHashCode(object);
    }
}