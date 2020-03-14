package conglin.clrpc.service.fallback;

public interface FallbackHolder {

    /**
     * 添加指定的 fallback
     * 
     * @param key
     * @param interfaceClass
     * @return
     */
    boolean add(String key, Class<?> interfaceClass);

    /**
     * 调用 Fallback
     * 
     * 使用该方法前应当使用 {@link FallbackHolder#enable()} 确认当前是否开启
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