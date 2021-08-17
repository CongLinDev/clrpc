package conglin.clrpc.common.registry;

import conglin.clrpc.router.instance.ServiceInstance;

import java.util.Collection;
import java.util.function.BiConsumer;

public interface DiscoveryCallback extends BiConsumer<String, Collection<ServiceInstance>> {

}
