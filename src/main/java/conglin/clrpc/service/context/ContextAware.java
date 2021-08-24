package conglin.clrpc.service.context;

public interface ContextAware {

    /**
     * 设置上下文
     *
     * @param context
     */
    void setContext(RpcContext context);

    /**
     * 获取上下文
     *
     * @return
     */
    RpcContext getContext();
}
