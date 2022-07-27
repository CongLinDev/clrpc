package conglin.clrpc.bootstrap.option;

import conglin.clrpc.invocation.identifier.IdentifierGenerator;
import conglin.clrpc.invocation.protocol.ProtocolDefinition;
import conglin.clrpc.invocation.serialization.SerializationHandler;
import conglin.clrpc.service.instance.codec.ServiceInstanceCodec;

public class BootOption {

    private IdentifierGenerator identifierGenerator;
    private SerializationHandler serializationHandler;
    private ServiceInstanceCodec serviceInstanceCodec;
    private ProtocolDefinition protocolDefinition;

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

    public ProtocolDefinition protocolDefinition() {
        return protocolDefinition;
    }

    public BootOption protocolDefinition(ProtocolDefinition protocolDefinition) {
        this.protocolDefinition = protocolDefinition;
        return this;
    }
}
