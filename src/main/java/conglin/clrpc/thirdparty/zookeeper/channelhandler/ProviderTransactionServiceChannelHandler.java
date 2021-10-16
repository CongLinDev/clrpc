package conglin.clrpc.thirdparty.zookeeper.channelhandler;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.extension.transaction.*;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.handler.ProviderAbstractServiceChannelHandler;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperTransactionHelper;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ProviderTransactionServiceChannelHandler
        extends ProviderAbstractServiceChannelHandler<TransactionRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTransactionServiceChannelHandler.class);

    protected TransactionHelper helper;

    @Override
    public void init() {
        PropertyConfigurer c = getContext().getWith(RpcContextEnum.PROPERTY_CONFIGURER);
        String urlString = c.get("atomicity.url", String.class);
        helper = new ZooKeeperTransactionHelper(new UrlScheme(urlString));
    }

    @Override
    protected Object execute(TransactionRequest msg) {
        // 标记事务的本条请求被当前服务提供者所占有
        long transactionId = msg.transactionId();
        int serialId = msg.serialId();
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
            ServiceObject serviceObject = findServiceBean(msg.serviceName());
            // 处理事务
            // 预提交事务
            TransactionResult transactionResult = jdkReflectInvoke(serviceObject.object(), msg);
            if (!helper.signSuccess(transactionId, serialId, serviceAddress)) { // 标记预提交成功
                // 标记操作失败的话直接丢弃请求
                return null;
            }
            // 监视节点
            helper.watch(transactionId, serialId, transactionResult.callback());
            // 发送预提交结果
            next(msg, new BasicResponse(msg.messageId(), transactionResult.result()));
            return null;
        } catch (UnsupportedServiceException e) {
            LOGGER.error("Transaction request(transactionId={} serialId={}) unsupported: {}", transactionId, serialId, e.getMessage());
            // 标记失败，未找到服务对象
            signFailed(transactionId, serialId, serviceAddress);
            return new BasicResponse(msg.messageId(), true, e);
        } catch (ServiceExecutionException e) {
            // 预提交失败
            LOGGER.error("Transaction request(transactionId={} serialId={}) execute failed: {}", transactionId, serialId, e.getMessage());
            // 标记失败，由服务消费者在下次定时轮询时重新请求
            signFailed(transactionId, serialId, serviceAddress);
            return new BasicResponse(msg.messageId(), true, e);
        } catch (TransactionException e) {
            // 预提交失败
            LOGGER.error("Transaction request(transactionId={} serialId={}) watch failed: {}", transactionId, serialId, e.getMessage());
            // 标记失败，由服务消费者在下次定时轮询时重新请求
            signFailed(transactionId, serialId, serviceAddress);
            return new BasicResponse(msg.messageId(), true, e);
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
    protected TransactionResult jdkReflectInvoke(Object serviceBean, BasicRequest request) throws ServiceExecutionException {
        Object result = super.jdkReflectInvoke(serviceBean, request);
        if (request instanceof TransactionResult) {
            return (TransactionResult) result;
        }
        return new CommonTransactionResult(result);
    }
}