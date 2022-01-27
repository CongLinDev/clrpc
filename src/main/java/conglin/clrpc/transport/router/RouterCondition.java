package conglin.clrpc.transport.router;

import java.util.function.Predicate;

import conglin.clrpc.service.instance.ServiceInstance;

public class RouterCondition {
    private int retryTimes;
    private Predicate<ServiceInstance> predicate;
    private String serviceName;
    private Integer random;

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Predicate<ServiceInstance> getPredicate() {
        if (predicate == null) {
            predicate = obj -> Boolean.TRUE;
        }
        return predicate;
    }

    public void setPredicate(Predicate<ServiceInstance> predicate) {
        this.predicate = predicate;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getRandom() {
        if (random == null) {
            random = 0;
        }
        return random;
    }

    public void setRandom(Integer random) {
        this.random = random;
    }
}
