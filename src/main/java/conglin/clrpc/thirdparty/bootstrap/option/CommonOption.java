package conglin.clrpc.thirdparty.bootstrap.option;

import conglin.clrpc.bootstrap.option.BootOption;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.identifier.SnowFlakeIdentifierGenerator;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.instance.codec.DefaultServiceInstanceCodec;
import conglin.clrpc.service.instance.codec.ServiceInstanceCodec;
import conglin.clrpc.thirdparty.protostuff.serialization.ProtostuffSerializationHandler;
import conglin.clrpc.transport.protocol.DefaultProtocolDefinition;
import conglin.clrpc.transport.protocol.ProtocolDefinition;

public class CommonOption extends BootOption {
    @Override
    public IdentifierGenerator identifierGenerator() {
        IdentifierGenerator object = super.identifierGenerator();
        if (object == null) {
            object = new SnowFlakeIdentifierGenerator(0);
            identifierGenerator(object);
        }
        return object;
    }

    @Override
    public SerializationHandler serializationHandler() {
        SerializationHandler object = super.serializationHandler();
        if (object == null) {
            object = new ProtostuffSerializationHandler();
            serializationHandler(object);
        }
        return object;
    }

    @Override
    public ServiceInstanceCodec serviceInstanceCodec() {
        ServiceInstanceCodec object = super.serviceInstanceCodec();
        if (object == null) {
            object = new DefaultServiceInstanceCodec();
            serviceInstanceCodec(object);
        }
        return object;
    }

    @Override
    public ProtocolDefinition protocolDefinition() {
        ProtocolDefinition object = super.protocolDefinition();
        if (object == null) {
            object = new DefaultProtocolDefinition();
            protocolDefinition(object);
        }
        return object;
    }
}
