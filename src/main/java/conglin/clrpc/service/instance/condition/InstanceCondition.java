package conglin.clrpc.service.instance.condition;

import java.util.function.Predicate;

import conglin.clrpc.service.ServiceVersion;
import conglin.clrpc.service.instance.ServiceInstance;

public interface InstanceCondition extends Predicate<ServiceInstance> {
    // PERMIT_ALL
    final static InstanceCondition PERMIT_ALL = new InstanceCondition() {
        @Override
        public boolean test(ServiceInstance t) {
            return true;
        }

        @Override
        public void setMinVersion(ServiceVersion min) {
            
        }
    };


    /**
     * 设置最小版本号
     * 
     * @param min
     */
    void setMinVersion(ServiceVersion min);
}
