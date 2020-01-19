package conglin.clrpc.transport.component;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicRequest;

public class DefaultRequestSender implements RequestSender {

    protected final FuturesHolder<Long> futuresHolder;

    protected final ProviderChooser providerChooser;

    protected final ExecutorService threadPool;

    public DefaultRequestSender(FuturesHolder<Long> futuresHolder, ProviderChooser providerChooser, ExecutorService threadPool) {
        this.futuresHolder = futuresHolder;
        this.providerChooser = providerChooser;
        this.threadPool = threadPool;
    }

    @Override
    public RpcFuture sendRequest(BasicRequest request) {
        RpcFuture future = putFuture(request);
        doSendRequest(request);
        return future;
    }

    @Override
    public RpcFuture sendRequest(String remoteAddress, BasicRequest request) {
        RpcFuture future = putFuture(request);
        doSendRequest(request, remoteAddress);
        return future;
    }

    @Override
    public void resendRequest(BasicRequest request) {
        doSendRequest(request);
    }

    @Override
    public void resendRequest(String remoteAddress, BasicRequest request) {
        doSendRequest(request, remoteAddress);
    }

    /**
     * 保存Future
     * 
     * @param request
     * @return
     */
    protected RpcFuture putFuture(BasicRequest request) {
        RpcFuture future = new BasicFuture(this, request);
        futuresHolder.putFuture(future.identifier(), future);
        return future;
    }

    /**
     * 发送请求
     * 
     * @param request
     */
    protected void doSendRequest(BasicRequest request) {
        String serviceName = request.getServiceName();
        providerChooser.choose(serviceName, request).pipeline().fireChannelRead(request);
    }

    /**
     * 发送请求
     * 
     * @param request
     * @param targetAddress
     */
    protected void doSendRequest(BasicRequest request, String targetAddress) {
        String serviceName = request.getServiceName();
        providerChooser.choose(serviceName, request).pipeline().fireChannelRead(request);
    }

}