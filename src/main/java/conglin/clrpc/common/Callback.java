package conglin.clrpc.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.service.proxy.ObjectProxy;

/**
 * 回调接口
 */
public interface Callback{

    /**
     * 成功
     * @param result
     */
    void success(Object result);

    /**
     * 失败
     * 失败后可以调用 {@link ObjectProxy#call(String, String, Object...)} 或
     * {@link ObjectProxy#call(String, java.lang.reflect.Method, Object...)} 方法
     * 来进行重试或回滚
     * @param remoteAddress 服务提供者地址
     * @param e 抛出的异常
     */
    void fail(String remoteAddress, RpcServiceException e);



    static final Logger log = LoggerFactory.getLogger(Callback.class);
    /**
     * 这是一个基础的 {@code Callback} 实例
     */
    Callback BASIC_CALLBACK = new Callback(){
    
        @Override
        public void success(Object result) {
            log.debug("Get result successfully. Result=" + result);
        }
    
        @Override
        public void fail(String remoteAddress, RpcServiceException e) {
            log.error(remoteAddress + ": " + e.getMessage());
        }
    };

    /**
     * 这是一个空的 {@code Callback} 实例
     */
    Callback EMPTY_CALLBACK = new Callback(){
    
        @Override
        public void success(Object result) {
            log.debug("EMPTY_CALLBACK: SUCCESSED.");
            
        }
    
        @Override
        public void fail(String remoteAddress, RpcServiceException e) {
            log.error("EMPTY_CALLBACK: FAILED.");
        }
    };
}