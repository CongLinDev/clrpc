package conglin.clrpc.service.instance.condition;

import conglin.clrpc.service.ServiceVersion;
import conglin.clrpc.service.instance.ServiceInstance;

public class DefaultInstanceCondition implements InstanceCondition {

    private ServiceVersion minVersion;

   
    @Override
    public void setMinVersion(ServiceVersion min) {
        this.minVersion = min;
    }

    @Override
    public boolean test(ServiceInstance t) {
        if (t == null) return false;
        if (minVersion == null) return true;
        return minVersion.compareTo(t.version()) <= 0;
    }
    
}
