package conglin.clrpc.extension.transaction.channelhandler;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.transaction.CommonTransactionResult;
import conglin.clrpc.extension.transaction.TransactionException;
import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.TransactionResult;
import conglin.clrpc.extension.transaction.payload.TransactionRequestPayload;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.handler.ProviderAbstractServiceChannelHandler;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.protocol.ProtocolDefinition;
import io.netty.channel.ChannelHandlerContext;

abstract public class AbstractProviderTransactionServiceChannelHandler extends ProviderAbstractServiceChannelHandler
        implements Initializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractProviderTransactionServiceChannelHandler.class);

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

        if (!isOccupied(transactionId, serialId, instanceId)) {
            // 直接丢弃
            LOGGER.debug("Ignore transaction request(transactionId={} serialId={})", transactionId, serialId);
            return null;
        }

        // 开始处理请求
        LOGGER.debug("Transaction request(transactionId={} serialId={}) will be executed.", transactionId, serialId);
        TransactionResult transactionResult = null;
        try {
            // 预提交事务
            transactionResult = buildTransactionResult(invoke(request));
        } catch (UnsupportedServiceException | ServiceExecutionException e) {
            // 预提交失败
            LOGGER.error("Transaction request(transactionId={} serialId={}) execute failed: {}", transactionId,
                    serialId, e.getMessage());
            if (signAbort(transactionId, serialId, instanceId)) {
                return new ResponsePayload(true, e);
            }
            return null; // 直接丢弃，等待重试
        }

        // 标记预提交成功
        if (!signPrecommit(transactionId, serialId, instanceId)) {
            transactionResult.callback().fail(null);
            return null; // 直接丢弃，等待重试
        }

        try {
            // 监视节点
            helper.watch(transactionId, transactionResult.callback().andThen(
                    new Callback() {
                        public void success(Object result) {
                            signCommit(transactionId, serialId, instanceId);
                        };

                        public void fail(Exception exception) {
                            signAbort(transactionId, serialId, instanceId);
                        };
                    }));
            // 发送预提交结果
            return new ResponsePayload(transactionResult.result());
        } catch (TransactionException e) {
            // 监视失败
            LOGGER.error("Transaction request(transactionId={} serialId={}) watch failed: {}", transactionId, serialId,
                    e.getMessage());
            transactionResult.callback().fail(null);
            return new ResponsePayload(true, e); // 返回失败信息
        }
    }

    private boolean signCommit(long transactionId, int serialId, String target) {
        try {
            return helper.signCommit(transactionId, serialId, target);
        } catch (TransactionException e) {
            LOGGER.warn("Transaction request(transactionId={} serialId={}) signCommit failed: {}", transactionId,
                    serialId, e.getMessage());
            return false;
        }
    }

    private boolean signAbort(long transactionId, int serialId, String target) {
        try {
            return helper.signAbort(transactionId, serialId, target);
        } catch (TransactionException e) {
            LOGGER.warn("Transaction request(transactionId={} serialId={}) signAbort failed: {}", transactionId,
                    serialId, e.getMessage());
            return false;
        }
    }

    private boolean signPrecommit(long transactionId, int serialId, String target) {
        try {
            return helper.signPrecommit(transactionId, serialId, target);
        } catch (TransactionException e) {
            LOGGER.warn("Transaction request(transactionId={} serialId={}) signPrecommit failed: {}", transactionId,
                    serialId, e.getMessage());
            return false;
        }
    }

    private boolean isOccupied(long transactionId, int serialId, String target) {
        try {
            return helper.isOccupied(transactionId, serialId, target);
        } catch (TransactionException e) {
            LOGGER.warn("Transaction request(transactionId={} serialId={}) isOccupied failed: {}", transactionId,
                    serialId, e.getMessage());
            return false;
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
