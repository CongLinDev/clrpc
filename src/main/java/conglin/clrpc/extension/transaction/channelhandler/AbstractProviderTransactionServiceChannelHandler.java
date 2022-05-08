package conglin.clrpc.extension.transaction.channelhandler;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.transaction.CommonTransactionResult;
import conglin.clrpc.extension.transaction.TransactionException;
import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.TransactionRequestPayload;
import conglin.clrpc.extension.transaction.TransactionResult;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.handler.ProviderAbstractServiceChannelHandler;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.protocol.ProtocolDefinition;
import io.netty.channel.ChannelHandlerContext;

abstract public class AbstractProviderTransactionServiceChannelHandler extends ProviderAbstractServiceChannelHandler implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProviderTransactionServiceChannelHandler.class);

    protected TransactionHelper helper;

    protected String instanceId;

    @Override
    public void init() {
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        this.instanceId = properties.getProperty("provider.instance.id");
        String urlString = properties.getProperty("extension.atomicity.url");
        helper = newTransactionHelper(new UrlScheme(urlString));
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
        try {
            if (!helper.isOccupied(transactionId, serialId, instanceId)) { // 是否占有该请求处理权限
                // 直接丢弃
                LOGGER.debug("Ignore transaction request(transactionId={} serialId={})", transactionId, serialId);
                return null;
            }
            // 开始处理请求
            LOGGER.debug("Transaction request(transactionId={} serialId={}) will be executed.", transactionId, serialId);

            // 预提交事务
            TransactionResult transactionResult = buildTransactionResult(invoke(request));

            if (!helper.signSuccess(transactionId, serialId, instanceId)) { // 标记预提交成功
                // 标记操作失败的话直接丢弃请求
                return null;
            }
            // 监视节点
            helper.watch(transactionId, transactionResult.callback());
            // 发送预提交结果
            return new ResponsePayload(transactionResult.result());
        } catch (UnsupportedServiceException e) {
            LOGGER.error("Transaction request(transactionId={} serialId={}) unsupported: {}", transactionId, serialId, e.getMessage());
            // 标记失败，未找到服务对象
            signFailed(transactionId, serialId, instanceId);
            return new ResponsePayload(true, e);
        } catch (ServiceExecutionException e) {
            // 预提交失败
            LOGGER.error("Transaction request(transactionId={} serialId={}) execute failed: {}", transactionId, serialId, e.getMessage());
            // 标记失败，由服务消费者在下次定时轮询时重新请求
            signFailed(transactionId, serialId, instanceId);
            return new ResponsePayload(true, e);
        } catch (TransactionException e) {
            // 预提交失败
            LOGGER.error("Transaction request(transactionId={} serialId={}) watch failed: {}", transactionId, serialId, e.getMessage());
            // 标记失败，由服务消费者在下次定时轮询时重新请求
            signFailed(transactionId, serialId, instanceId);
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

    /**
     * 构建 {@link TransactionResult}
     * 
     * @param object
     * @return
     */
    protected TransactionResult buildTransactionResult(Object object) {
        if (object instanceof TransactionResult) {
            return (TransactionResult) object;
        }
        return new CommonTransactionResult(object);
    }

    /**
     * 创建一个 {@link TransactionHelper}
     * 
     * @param urlScheme
     * @return
     */
    abstract protected TransactionHelper newTransactionHelper(UrlScheme urlScheme);
}
