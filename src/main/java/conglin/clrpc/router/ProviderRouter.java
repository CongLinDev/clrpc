package conglin.clrpc.router;

import conglin.clrpc.common.loadbalance.LoadBalancer;
import conglin.clrpc.common.object.Pair;
import conglin.clrpc.router.instance.ServiceInstance;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderRouter implements Router<ServiceInstance, Channel> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProviderRouter.class);
    private final static int TIMEOUT_FOR_WAIT = 5000;

    private final LoadBalancer<String, ServiceInstance, Channel> loadBalancer;

    public ProviderRouter(LoadBalancer<String, ServiceInstance, Channel> loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public Pair<ServiceInstance, Channel> choose(RouterCondition<ServiceInstance> condition) throws NoAvailableServiceInstancesException {
        String serviceName = condition.getServiceName();
        // 不断尝试
        int retryTimes = condition.getRetryTimes();
        int retryCount = 0;
        while (retryCount++ < retryTimes) {
            Pair<ServiceInstance, Channel> pair = loadBalancer.getEntity(serviceName, condition.getRandom(), condition.getPredicate());
            if (pair != null)
                return pair;
            try {
                LOGGER.debug("Wait for available service=({}) provider {} ms ...", serviceName, TIMEOUT_FOR_WAIT);
                synchronized (this) {
                    wait(TIMEOUT_FOR_WAIT);
                }
            } catch (InterruptedException e) {
                LOGGER.error("Waiting for available provider(serviceName={}) is interrupted!", serviceName, e);
            }
            LOGGER.warn("Waiting for available provider(serviceName={}). retryTimes={}", serviceName, retryCount);
        }
        throw new NoAvailableServiceInstancesException(condition);
    }

    @Override
    public synchronized void refresh() {
        notifyAll();
    }
}
