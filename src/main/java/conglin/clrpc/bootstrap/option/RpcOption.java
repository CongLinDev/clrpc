package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.instance.ServiceInstanceCodec;

public class RpcOption {

    private IdentifierGenerator identifierGenerator;
    private SerializationHandler serializationHandler;
    private ServiceInstanceCodec serviceInstanceCodec;

    public IdentifierGenerator identifierGenerator() {
        return identifierGenerator;
    }

    public RpcOption identifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
        return this;
    }

    public SerializationHandler serializationHandler() {
        return serializationHandler;
    }

    public RpcOption serializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        return this;
    }

    public ServiceInstanceCodec serviceInstanceCodec() {
        return serviceInstanceCodec;
    }

    public RpcOption serviceInstanceCodec(ServiceInstanceCodec serviceInstanceCodec) {
        this.serviceInstanceCodec = serviceInstanceCodec;
        return this;
    }
}
