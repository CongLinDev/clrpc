package conglin.clrpc.thirdparty.zookeeper.channelhandler;

import java.net.InetSocketAddress;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.extension.transaction.CommonTransactionResult;
import conglin.clrpc.extension.transaction.TransactionException;
import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.TransactionRequestPayload;
import conglin.clrpc.extension.transaction.TransactionResult;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.handler.ProviderAbstractServiceChannelHandler;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperTransactionHelper;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.RequestPayload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.protocol.ProtocolDefinition;
import io.netty.channel.ChannelHandlerContext;

public class ProviderTransactionServiceChannelHandler extends ProviderAbstractServiceChannelHandler implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTransactionServiceChannelHandler.class);

    protected TransactionHelper helper;

    @Override
    public void init() {
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        String urlString = properties.getProperty("extension.atomicity.url");
        helper = new ZooKeeperTransactionHelper(new UrlScheme(urlString));
        ProtocolDefinition protocolDefinition = getContext().getWith(ComponentContextEnum.PROTOCOL_DEFINITION);
        protocolDefinition.setPayloadType(TransactionRequestPayload.PAYLOAD_TYPE, TransactionRequestPayload.class);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ObjectLifecycleUtils.destroy(helper);
    }

    @Override
    protected boolean accept(Payload payload) {
        return payload instanceof TransactionRequestPayload;
    }

    @Override
    protected ResponsePayload execute(Payload payload) {
        // 标记事务的本条请求被当前服务提供者所占有
        TransactionRequestPayload request = (TransactionRequestPayload) payload;
        long transactionId = request.transactionId();
        int serialId = request.serialId();
        LOGGER.debug("Receive transaction request(transactionId={} serialId={})", transactionId, serialId);
        final String serviceAddress = IPAddressUtils.addressString((InetSocketAddress) pipeline().channel().localAddress());
        try {
            if (!helper.isOccupied(transactionId, serialId, serviceAddress)) { // 是否占有该请求处理权限
                // 直接丢弃
                LOGGER.debug("Ignore transaction request(transactionId={} serialId={})", transactionId, serialId);
                return null;
            }
            // 开始处理请求
            LOGGER.debug("Transaction request(transactionId={} serialId={}) will be executed.", transactionId, serialId);
            // 查询服务对象
            ServiceObject<?> serviceObject = findServiceBean(request.serviceName());
            // 处理事务
            // 预提交事务
            TransactionResult transactionResult = jdkReflectInvoke(serviceObject.object(), request);
            if (!helper.signSuccess(transactionId, serialId, serviceAddress)) { // 标记预提交成功
                // 标记操作失败的话直接丢弃请求
                return null;
            }
            // 监视节点
            helper.watch(transactionId, serialId, transactionResult.callback());
            // 发送预提交结果
            return new ResponsePayload(transactionResult.result());
        } catch (UnsupportedServiceException e) {
            LOGGER.error("Transaction request(transactionId={} serialId={}) unsupported: {}", transactionId, serialId, e.getMessage());
            // 标记失败，未找到服务对象
            signFailed(transactionId, serialId, serviceAddress);
            return new ResponsePayload(true, e);
        } catch (ServiceExecutionException e) {
            // 预提交失败
            LOGGER.error("Transaction request(transactionId={} serialId={}) execute failed: {}", transactionId, serialId, e.getMessage());
            // 标记失败，由服务消费者在下次定时轮询时重新请求
            signFailed(transactionId, serialId, serviceAddress);
            return new ResponsePayload(true, e);
        } catch (TransactionException e) {
            // 预提交失败
            LOGGER.error("Transaction request(transactionId={} serialId={}) watch failed: {}", transactionId, serialId, e.getMessage());
            // 标记失败，由服务消费者在下次定时轮询时重新请求
            signFailed(transactionId, serialId, serviceAddress);
            return new ResponsePayload(true, e);
        }
    }

    private void signFailed(long transactionId, int serialId, String target) {
        try {
            helper.signFailed(transactionId, serialId, target);
        } catch (TransactionException e) {
            LOGGER.error("Transaction request(transactionId={} serialId={}) signFailed failed: {}", transactionId, serialId, e.getMessage());
        }
    }

    @Override
    protected TransactionResult jdkReflectInvoke(Object serviceBean, RequestPayload request) throws ServiceExecutionException {
        Object result = super.jdkReflectInvoke(serviceBean, request);
        if (result instanceof TransactionResult) {
            return (TransactionResult) result;
        }
        return new CommonTransactionResult(result);
    }
}