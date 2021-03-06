package conglin.clrpc.service.handler;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.CallbackFunction;
import conglin.clrpc.common.Url;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.common.util.TransactionHelper;
import conglin.clrpc.service.annotation.AnnotationParser;
import conglin.clrpc.service.context.channel.ProviderChannelContext;
import conglin.clrpc.transport.message.BasicResponse;
import conglin.clrpc.transport.message.TransactionRequest;
import conglin.clrpc.zookeeper.util.ZooKeeperTransactionHelper;

public class ProviderTransactionServiceChannelHandler
        extends ProviderAbstractServiceChannelHandler<TransactionRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTransactionServiceChannelHandler.class);

    protected final TransactionHelper helper;

    public ProviderTransactionServiceChannelHandler(ProviderChannelContext context) {
        super(context);
        String urlString = context.propertyConfigurer().get("atomicity", String.class);
        helper = new ZooKeeperTransactionHelper(new Url(urlString));
    }

    @Override
    protected Object execute(TransactionRequest msg) {

        // 标记事务的本条请求被当前服务提供者所占有
        long transactionId = msg.getTransactionId();
        int serialId = msg.getSerialId();
        LOGGER.debug("Receive transaction request(transactionId={} serialId={})", transactionId, serialId);

        if (!helper.sign(transactionId, serialId)) { // 标记占有该原子消息的处理权
            LOGGER.debug("Ignore transaction request(transactionId={} serialId={})", transactionId, serialId);
            return null;
        }

        LOGGER.debug("Transaction request(transactionId={} serialId={}) will be executed.", transactionId, serialId);

        // 查询服务对象
        Object serviceBean = null;
        try {
            serviceBean = findServiceBean(msg.serviceName());
        } catch (UnsupportedServiceException e) {
            LOGGER.error("UnsupportedService: {}", e.getMessage());
            // 重新标记，由服务消费者在下次定时轮询时重新请求
            helper.reprepare(transactionId, serialId);
            return null;
        }

        try {
            // 预提交
            Class<?> clazz = serviceBean.getClass();
            Method method = clazz.getMethod(msg.methodName(), ClassUtils.getClasses(msg.parameters()));
            Method precommitMethod = AnnotationParser.precommitMethod(method);

            final boolean isTrans = precommitMethod != null;

            Object result = isTrans ? jdkReflectInvoke(serviceBean, precommitMethod, msg.parameters())
                    : jdkReflectInvoke(serviceBean, method, msg.parameters());
            helper.precommit(transactionId, serialId);

            // 预提交成功后，标记预提交成功并监视上一个节点
            // 若顺序执行，则监视上一个子节点，反之监视事务节点
            helper.watch(transactionId, (msg.isSerial() ? serialId - 1 : 0), new Callback() {
                @Override
                public void success(Object r) {
                    if (isTrans) {
                        CallbackFunction callback = (CallbackFunction) result;
                        next(msg, new BasicResponse(msg.messageId(), callback.apply(Boolean.TRUE)));
                    } else {
                        next(msg, new BasicResponse(msg.messageId(), result));
                    }
                    helper.commit(transactionId, serialId);
                    LOGGER.debug("Transaction request(transactionId={} serialId={}) has been commited.", transactionId,
                            serialId);
                }

                @Override
                public void fail(Exception exception) {
                    if (isTrans) {
                        CallbackFunction callback = (CallbackFunction) result;
                        callback.apply(Boolean.FALSE);
                    }
                    next(msg, new BasicResponse(msg.messageId(), true,
                            new ServiceExecutionException("Transaction has been cancelled.")));
                    helper.abort(transactionId, serialId);
                    LOGGER.debug("Transaction request(transactionId={} serialId={}) has been cancelled.", transactionId,
                            serialId);
                }
            });
        } catch (ServiceExecutionException e) {
            // 预提交执行错误，则直接标记错误
            helper.abort(transactionId, serialId);
            LOGGER.error("Precommit failed. Transaction request(transactionId={} serialId={}). Cause: {}",
                    transactionId, serialId, e.getMessage());
            next(msg, new BasicResponse(msg.messageId(), true, e));
        } catch (NoSuchMethodException | SecurityException e) {
            // will not happen
        }
        return null;
    }
}