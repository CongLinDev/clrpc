package conglin.clrpc.router;

import java.util.function.Predicate;

public class RouterCondition<T> {
    private int retryTimes;
    private Predicate<T> predicate;
    private String serviceName;
    private Integer random;

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Predicate<T> getPredicate() {
        if (predicate == null) {
            predicate = obj -> Boolean.TRUE;
        }
        return predicate;
    }

    public void setPredicate(Predicate<T> predicate) {
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
