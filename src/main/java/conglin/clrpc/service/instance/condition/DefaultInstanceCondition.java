package conglin.clrpc.service.instance.condition;

import conglin.clrpc.service.ServiceVersion;
import conglin.clrpc.service.instance.ServiceInstance;

public class DefaultInstanceCondition implements InstanceCondition {

    private ServiceVersion currentVersion;

   
    @Override
    public void currentVersion(ServiceVersion currentVersion) {
        this.currentVersion = currentVersion;
    }

    @Override
    public boolean test(ServiceInstance t) {
        if (t == null) return false;
        if (currentVersion == null) return true;
        return currentVersion.compareTo(t.version()) <= 0;
    }
    
}
