package conglin.clrpc.thirdparty.bootstrap.option;

import conglin.clrpc.bootstrap.option.RpcOption;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.identifier.RandomIdentifierGenerator;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.router.instance.ServiceInstanceGenerator;
import conglin.clrpc.thirdparty.fastjson.service.JsonServiceInstance;
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
    public ServiceInstanceGenerator serviceInstanceGenerator() {
        ServiceInstanceGenerator object = super.serviceInstanceGenerator();
        if (object == null) {
            object = JsonServiceInstance::new;
            serviceInstanceGenerator(object);
        }
        return object;
    }
}
