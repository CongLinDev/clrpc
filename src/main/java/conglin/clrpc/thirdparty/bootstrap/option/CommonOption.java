package conglin.clrpc.thirdparty.bootstrap.option;

import conglin.clrpc.bootstrap.option.RpcOption;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.identifier.RandomIdentifierGenerator;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.instance.DefaultServiceInstanceCodec;
import conglin.clrpc.service.instance.ServiceInstanceCodec;
import conglin.clrpc.thirdparty.protostuff.serialization.ProtostuffSerializationHandler;

public class CommonOption extends RpcOption {
    @Override
    public IdentifierGenerator identifierGenerator() {
        IdentifierGenerator object = super.identifierGenerator();
        if (object == null) {
            object = new RandomIdentifierGenerator();
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
}
