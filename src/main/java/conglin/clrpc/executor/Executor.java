package conglin.clrpc.executor;

public interface Executor {

    /**
     * 处理入站数据
     * 
     * @param object
     * @return
     */
    void inbound(Object object);

    /**
     * 处理出站数据
     * 
     * @param object
     * @return
     */
    void outbound(Object object);
}
