package conglin.clrpc.thirdparty.zookeeper.registry;

import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.registry.DiscoveryCallback;
import conglin.clrpc.router.instance.ServiceInstance;

import java.util.Collection;
import java.util.function.Consumer;

public class DiscoveryCallbackConvertor implements Consumer<Collection<Pair<String, String>>> {

    private final DiscoveryCallback discoveryCallback;

    private final String type;

    public DiscoveryCallbackConvertor(String type, DiscoveryCallback discoveryCallback) {
        this.type = type;
        this.discoveryCallback = discoveryCallback;
    }

    @Override
    public void accept(Collection<Pair<String, String>> pairs) {
        // TODO: pairs -> instances
        Collection<ServiceInstance> instances = null;
        discoveryCallback.accept(type, instances);
    }
}
