package conglin.clrpc.service.router;

import conglin.clrpc.service.instance.condition.EmptyInstanceCondition;
import conglin.clrpc.service.instance.condition.InstanceCondition;

public class RouterCondition {
    private InstanceCondition instanceCondition;
    private String serviceName;
    private int random;
    public InstanceCondition getInstanceCondition() {
        if (instanceCondition == null) {
            instanceCondition = new EmptyInstanceCondition();
        }
        return instanceCondition;
    }

    public void setInstanceCondition(InstanceCondition instanceCondition) {
        this.instanceCondition = instanceCondition;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getRandom() {
        return random;
    }

    public void setRandom(int random) {
        this.random = random;
    }
}
