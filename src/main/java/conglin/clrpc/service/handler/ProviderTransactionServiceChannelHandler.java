package conglin.clrpc.service.handler;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.common.util.TransactionHelper;
import conglin.clrpc.service.annotation.AnnotationParser;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.message.BasicResponse;
import conglin.clrpc.transport.message.TransactionRequest;
import conglin.clrpc.zookeeper.util.ZooKeeperTransactionHelper;

public class ProviderTransactionServiceChannelHandler
        extends ProviderAbstractServiceChannelHandler<TransactionRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTransactionServiceChannelHandler.class);

    protected final TransactionHelper helper;

    public ProviderTransactionServiceChannelHandler(ProviderContext context) {
        super(context);
        helper = new ZooKeeperTransactionHelper(context.getPropertyConfigurer());
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
            serviceBean = findServiceBean(msg.getServiceName());
        } catch (UnsupportedServiceException e) {
            LOGGER.error("UnsupportedService: {}", e.getMessage());
            // 重新标记，由服务消费者在下次定时轮询时重新请求
            helper.reprepare(transactionId, serialId);
            return null;
        }

        try {
            final Object result = jdkReflectInvoke(serviceBean, msg); // 预提交
            // 预提交成功后，标记预提交成功并监视上一个节点
            helper.precommit(transactionId, serialId);

            Class<?> clazz = serviceBean.getClass();
            // 若顺序执行，则监视上一个子节点，反之监视事务节点
            helper.watch(transactionId, (msg.isSerial() ? serialId - 1 : 0), new Callback() {
                @Override
                public void success(Object res) { // 执行提交操作
                    Object commitResult = null;
                    try {
                        Method method = clazz.getMethod(msg.getMethodName(),
                                ClassUtils.getClasses(msg.getParameters()));
                        String commit = AnnotationParser.resolveTransactionCommit(method);

                        if (commit != null)
                            commitResult = ClassUtils.reflectInvoke(clazz, commit, result);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }

                    helper.commit(transactionId, serialId);
                    LOGGER.debug("Transaction request(transactionId={} serialId={}) has been commited.", transactionId,
                            serialId);

                    BasicResponse response = new BasicResponse(msg.getMessageId());
                    response.setResult(commitResult == null ? result : commitResult);
                    next(msg, response);
                }

                @Override
                public void fail(Exception exception) { // 执行回滚操作
                    // 返回错误回复
                    try {
                        Method method = clazz.getMethod(msg.getMethodName(),
                                ClassUtils.getClasses(msg.getParameters()));
                        String rollback = AnnotationParser.resolveTransactionRollback(method);

                        if (rollback != null)
                            ClassUtils.reflectInvoke(clazz, rollback, result);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                    helper.abort(transactionId, serialId);
                    LOGGER.debug("Transaction request(transactionId={} serialId={}) has been canceled.", transactionId,
                            serialId);

                    BasicResponse response = new BasicResponse(msg.getMessageId(), true);
                    response.setResult(new ServiceExecutionException("Transaction has been cancelled."));
                    next(msg, response);
                }
            });
        } catch (ServiceExecutionException e) {
            // 预提交执行错误，则直接标记错误
            helper.abort(transactionId, serialId);
            LOGGER.error("Precommit failed. Transaction request(transactionId={} serialId={}). Cause: {}",
                    transactionId, serialId, e.getMessage());
            BasicResponse response = new BasicResponse(msg.getMessageId(), true);
            response.setResult(e);
            next(msg, response);
        }
        return null;
    }
}