package conglin.clrpc.service.fallback;

public interface FallbackHolder {

    /**
     * 直接添加 fallback 对象
     * 
     * @param key
     * @param fallback
     * @return
     */
    Object put(String key, Object fallback);

    /**
     * 添加指定的 fallback
     * 
     * @param key
     * @param interfaceClass
     * @return
     */
    boolean add(String key, Class<?> interfaceClass);

    /**
     * 获取 fallback 对象
     * 
     * @param key
     * @return
     */
    Object get(String key);

    /**
     * 调用 Fallback
     * 
     * 使用该方法前应当使用 {@link FallbackHolder#mode()} 确认当前模式
     * 
     * @param service
     * @param methodName
     * @param args
     * @return
     * @throws FallbackFailedException
     */
    Object fallback(String service, String methodName, Object[] args) throws FallbackFailedException;

    /**
     * 是否开启
     * 
     * @return
     */
    boolean enable();

    /**
     * 是否需要 fallback
     * 
     * @param retryTimes
     * @return
     */
    boolean needFallback(int retryTimes);
}