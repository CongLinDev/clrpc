package conglin.clrpc.transfer.sender;

import java.util.function.BiFunction;

import conglin.clrpc.common.exception.NoSuchProviderException;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.channel.Channel;

public interface RequestSender extends Runnable{

    /**
     * 初始化函数
     * @param serviceHandler
     * @param providerChooser 服务提供者选择器 
     *                        第一个参数为服务名
     *                        第二个参数类型若是String，则认为是按照指定的url进行选择。若不是，则按照策略选择。
     *                        返回值为 {@link io.netty.channel.Channel} 使用该对象发送消息
     */
    void init(ConsumerServiceHandler serviceHandler, BiFunction<String, Object, Channel> providerChooser);


    /**
     * 绑定缓冲池
     * @param cacheManager
     */
    void bindCachePool(CacheManager<BasicRequest, BasicResponse> cacheManager);

    /**
     * 发送请求
     * @param request
     * @return
     */
    RpcFuture sendRequest(BasicRequest request);

    /**
     * 发送请求
     * 指定远端地址
     * @param remoteAddress
     * @param request
     * @return
     * @throws NoSuchProviderException
     */
    RpcFuture sendRequest(String remoteAddress, BasicRequest request) throws NoSuchProviderException;

    /**
     * 该方法仅用于未收到请求后的重试
     * 使用者应该使用 {@link RpcFuture#retry()} 进行重试
     * @param remoteAddress
     * @param request
     * @return
     * @throws NoSuchProviderException
     */
    void resendRequest(String remoteAddress, BasicRequest request) throws NoSuchProviderException;

    /**
     * 关闭发送器
     */
    void stop();
}