package conglin.clrpc.common.codec;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.codec.protostuff.ProtostuffSerializationHandler;
import conglin.clrpc.common.util.ConfigParser;

public class SerializationHandlerHolder {
    private static final Logger log = LoggerFactory.getLogger(SerializationHandlerHolder.class);

    public static SerializationHandler getHandler() {
        return SingletonHolder.SERIALIZATION_HANDLER;
    }

    private static class SingletonHolder {
        private static final SerializationHandler SERIALIZATION_HANDLER;

        static {
            String serializationHandlerName = ConfigParser.getOrDefault("service.codec.serialization-handler",
                    "conglin.clrpc.common.codec.protostuff.ProtostuffSerializationHandler");
            SerializationHandler handler = null;

            try {
                Class<?> clazz = Class.forName(serializationHandlerName);
                handler = SerializationHandler.class.cast(clazz.getConstructor().newInstance());
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                log.warn(e.getMessage()
                        + ". Loading 'conglin.clrpc.common.codec.protostuff.ProtostuffSerializationHandler' rather than "
                        + serializationHandlerName);
            } finally {
                SERIALIZATION_HANDLER = (handler == null) ? new ProtostuffSerializationHandler() : handler;
            }
        }
    }
}