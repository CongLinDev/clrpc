package conglin.clrpc.transport.router;

public interface Router {
    /**
     * choose
     *
     * @param condition
     * @return
     * @throws NoAvailableServiceInstancesException
     */
    RouterResult choose(RouterCondition condition) throws NoAvailableServiceInstancesException;


    /**
     * 订阅服务
     * 
     * @param serviceName
     */
    void subscribe(String serviceName);
}
