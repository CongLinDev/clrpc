package conglin.clrpc.transport.component;

/**
 * 服务提供者选择适配器
 *
 * 用于对负载均衡器的随机选择算法补充
 */
public interface ProviderChooserAdapter {
    /**
     * 自定义策略
     * 
     * @param requestId
     * @param methodName
     * @return
     */
    int apply(Long requestId, String methodName);

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