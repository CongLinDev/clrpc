package conglin.clrpc.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    void fail(String remoteAddress, Exception e);



    static final Logger log = LoggerFactory.getLogger(Callback.class);
    /**
     * 这是一个基础的 Callback 实例
     */
    Callback BASIC_CALLBACK = new Callback(){
    
        @Override
        public void success(Object result) {
            log.debug("Get result successfully. Result=" + result);
        }
    
        @Override
        public void fail(String remoteAddress, Exception e) {
            log.error(remoteAddress + ": " + e.getMessage());
        }
    };
}