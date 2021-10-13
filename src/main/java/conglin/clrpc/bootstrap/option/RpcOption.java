package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.router.instance.ServiceInstanceGenerator;
import conglin.clrpc.transport.component.ProviderChooserAdapter;

public class RpcOption {

    private IdentifierGenerator identifierGenerator;
    private ProviderChooserAdapter providerChooserAdapter;
    private SerializationHandler serializationHandler;
    private ServiceInstanceGenerator serviceInstanceGenerator;

    public IdentifierGenerator identifierGenerator() {
        return identifierGenerator;
    }

    public RpcOption identifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
        return this;
    }

    public ProviderChooserAdapter providerChooserAdapter() {
        return providerChooserAdapter;
    }

    public RpcOption providerChooserAdapter(ProviderChooserAdapter providerChooserAdapter) {
        this.providerChooserAdapter = providerChooserAdapter;
        return this;
    }

    public SerializationHandler serializationHandler() {
        return serializationHandler;
    }

    public RpcOption serializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        return this;
    }

    public ServiceInstanceGenerator serviceInstanceGenerator() {
        return serviceInstanceGenerator;
    }

    public RpcOption serviceInstanceGenerator(ServiceInstanceGenerator serviceInstanceGenerator) {
        this.serviceInstanceGenerator = serviceInstanceGenerator;
        return this;
    }
}
