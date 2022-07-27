package conglin.clrpc.invocation.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdkSerializationHandler implements SerializationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkSerializationHandler.class);

    @Override
    public <T> byte[] serialize(T t) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(t);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error("serialize failed {} cause", t, e);
        }
        return null;
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data); ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object object = objectInputStream.readObject();
            if (clazz.isAssignableFrom(object.getClass())) {
                @SuppressWarnings("unchecked")
                T result = (T) object;
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("deserialize failed. cause", e);
        }
        return null;
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] data, int offset, int length) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data, offset, length); ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object object = objectInputStream.readObject();
            if (clazz.isAssignableFrom(object.getClass())) {
                @SuppressWarnings("unchecked")
                T result = (T) object;
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("deserialize failed. cause", e);
        }
        return null;
    }
    
}
