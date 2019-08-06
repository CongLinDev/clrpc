package conglin.clrpc.common.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param e
     */
    void fail(Exception e);




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
        public void fail(Exception e) {
            log.error(e.getMessage());
        }
    };
}