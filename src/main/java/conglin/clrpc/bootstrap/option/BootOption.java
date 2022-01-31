package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.instance.ServiceInstanceCodec;

public class BootOption {

    private IdentifierGenerator identifierGenerator;
    private SerializationHandler serializationHandler;
    private ServiceInstanceCodec serviceInstanceCodec;

    public IdentifierGenerator identifierGenerator() {
        return identifierGenerator;
    }

    public BootOption identifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
        return this;
    }

    public SerializationHandler serializationHandler() {
        return serializationHandler;
    }

    public BootOption serializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        return this;
    }

    public ServiceInstanceCodec serviceInstanceCodec() {
        return serviceInstanceCodec;
    }

    public BootOption serviceInstanceCodec(ServiceInstanceCodec serviceInstanceCodec) {
        this.serviceInstanceCodec = serviceInstanceCodec;
        return this;
    }
}
