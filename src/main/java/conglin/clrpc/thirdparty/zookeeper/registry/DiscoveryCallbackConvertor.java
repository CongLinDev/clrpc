package conglin.clrpc.thirdparty.zookeeper.registry;

import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.registry.DiscoveryCallback;
import conglin.clrpc.router.instance.ServiceInstance;
import conglin.clrpc.thirdparty.fastjson.service.JsonServiceInstance;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DiscoveryCallbackConvertor implements Consumer<Collection<Pair<String, String>>> {

    private final DiscoveryCallback discoveryCallback;

    private final String type;

    public DiscoveryCallbackConvertor(String type, DiscoveryCallback discoveryCallback) {
        this.type = type;
        this.discoveryCallback = discoveryCallback;
    }

    @Override
    public void accept(Collection<Pair<String, String>> pairs) {
        Collection<ServiceInstance> instances = pairs.stream().map(pair-> JsonServiceInstance.fromContent(pair.getSecond())).collect(Collectors.toList());
        discoveryCallback.accept(type, instances);
    }
}
