package conglin.clrpc.transport.chooser;

import conglin.clrpc.transport.message.BasicRequest;

/**
 * 服务提供者选择适配器
 *
 * 用于对负载均衡器的随机选择算法补充
 */
public interface ProviderChooserAdapter {
    /**
     * 自定义策略
     * 
     * @param request
     * @return
     */
    int apply(final BasicRequest request);
}