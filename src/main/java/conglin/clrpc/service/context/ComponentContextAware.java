package conglin.clrpc.service.context;

public interface ComponentContextAware {

    /**
     * 设置上下文
     *
     * @param context
     */
    void setContext(ComponentContext context);

    /**
     * 获取上下文
     *
     * @return
     */
    ComponentContext getContext();
}
