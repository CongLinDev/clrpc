package conglin.clrpc.bootstrap;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.receiver.BasicResponseReceiver;
import conglin.clrpc.transfer.net.receiver.ResponseReceiver;
import conglin.clrpc.transfer.net.sender.BasicRequestSender;
import conglin.clrpc.transfer.net.sender.RequestSender;

/**
 * RPC client端启动类
 * 
 * 使用如下代码启动
 * <blockquote><pre>
 *     RpcClientBootstrap bootstrap = new RpcClientBootstrap();
 *     bootstrap.start();
 *     //同步服务
 *     Interface1 i1 = bootstrap.getService("service1");
 *     Interface2 i2 = bootstrap.getService(Interface2.class);
 *     //异步服务
 *     ObjectProxy proxy = bootstrap.getAsynchronousService("service3");
 *     
 * </pre></blockquote>
 * 
 * 注意：结束后不要忘记关闭客户端，释放资源。
 */

public class RpcClientBootstrap {

    private static final Logger log = LoggerFactory.getLogger(RpcClientBootstrap.class);

    private ClientTransfer clientTransfer;

    private ClientServiceHandler serviceHandler;

    public RpcClientBootstrap(){
        serviceHandler = new ClientServiceHandler();
        clientTransfer = new ClientTransfer();
    }

    /**
     * 获取同步服务代理
     * @param <T>
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public <T> T getService(Class<T> interfaceClass){
        return getService(interfaceClass, interfaceClass.getSimpleName());
    }

    /**
     * 获取同步服务代理
     * @param <T>
     * @param interfaceClass 接口类
     * @param serviceName 服务名
     * @return 返回代理服务类
     */
    public <T> T getService(Class<T> interfaceClass, String serviceName){
        clientTransfer.findService(serviceName);
        return serviceHandler.getService(interfaceClass, serviceName, clientTransfer.getSender());
    }

    /**
     * 获取异步服务代理
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public ObjectProxy getAsynchronousService(Class<?> interfaceClass){
        return getAsynchronousService(interfaceClass.getSimpleName());
    }

    /**
     * 获取异步服务代理
     * @param serviceName 返回代理服务类
     * @return 返回代理服务类
     */
    public ObjectProxy getAsynchronousService(String serviceName){
        clientTransfer.findService(serviceName);
        return serviceHandler.getAsynchronousService(serviceName, clientTransfer.getSender());
    }

    /**
     * 启动
     */
    public void start(){
        clientTransfer.start(initRequestSender(), initResponseReceiver());
        serviceHandler.start(clientTransfer.getSender());
    }

    /**
     * 停止
     */
    public void stop() throws InterruptedException{
        serviceHandler.stop();
        clientTransfer.stop();
    }

    /**
     * 获取一个 {@link RequestSender} 
     * 并调用 {@link RequestSender#init(ClientServiceHandler, ClientTransfer)} 进行初始化
     * @return
     */
    protected RequestSender initRequestSender(){
        String senderClassName = ConfigParser.getOrDefault("client.request-sender", "conglin.clrpc.transfer.net.sender.BasicRequestSender");
        RequestSender sender = null;
        try {
            sender = (RequestSender) Class.forName(senderClassName)
                    .getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.net.sender.BasicRequestSender' rather than "
                    + senderClassName);
        }finally{
            // 如果类名错误，则默认加载 {@link conglin.clrpc.transfer.net.sender.BasicRequestSender}
            sender = (sender == null) ? new BasicRequestSender() : sender;
        }

        sender.init(serviceHandler, clientTransfer);
        //serviceHandler.submit(sender);
        return sender;
    }

    protected ResponseReceiver initResponseReceiver(){
        String receiverClassName = ConfigParser.getOrDefault("client.response-receiver", "conglin.clrpc.transfer.net.receiver.BasicResponseReceiver");
        ResponseReceiver receiver = null;

        try {
            receiver = (ResponseReceiver) Class.forName(receiverClassName)
                    .getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.net.receiver.BasicResponseReceiver' rather than "
                    + receiverClassName);
        }finally{
            // 如果类名错误，则默认加载 {@link conglin.clrpc.transfer.net.receiver.BasicResponseReceiver}
            receiver = (receiver == null) ? new BasicResponseReceiver() : receiver;
        }

        receiver.init(serviceHandler);
        return receiver;
    }
}