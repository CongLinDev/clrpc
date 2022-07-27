package conglin.clrpc.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.invocation.UnsupportedServiceException;

public class ServiceObjectHolder {
    private final Map<String, InnerServiceWrapper> map;

    public ServiceObjectHolder() {
        this.map = new HashMap<>();
    }

    /**
     * 加入 {@link ServiceObject}
     * 
     * @param serviceObject
     * @return
     */
    public boolean putServiceObject(ServiceObject<?> serviceObject) {
        return map.putIfAbsent(serviceObject.name(), new InnerServiceWrapper(serviceObject)) == null;
    }

    /**
     * 获取 {@link ServiceObject}
     * 
     * @param serviceName
     * @return
     */
    public ServiceObject<?> getServiceObject(String serviceName) {
        InnerServiceWrapper wrapper = map.get(serviceName);
        if (wrapper == null)
            return null;
        return wrapper.getServiceObject();
    }

    /**
     * foreach
     * 
     * @param consumer
     */
    public void forEach(Consumer<ServiceObject<?>> consumer) {
        this.map.values().forEach(v -> consumer.accept(v.getServiceObject()));
    }

    /**
     * invoke
     * 
     * @param serviceName
     * @param methodName
     * @param parameters
     * @return
     * @throws UnsupportedServiceException
     * @throws ServiceExecutionException
     */
    public Object invoke(String serviceName, String methodName, Object[] parameters)
            throws UnsupportedServiceException, ServiceExecutionException {
        InnerServiceWrapper wrapper = map.get(serviceName);
        if (wrapper == null) {
            throw new UnsupportedServiceException(serviceName);
        }

        try {
            return wrapper.invoke(wrapper.getServiceObject().object(), serviceName, parameters);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ServiceExecutionException(e);
        }
    }

    private static class InnerServiceWrapper extends ServiceMethodWrapper {
        private final ServiceObject<?> serviceObject;

        public InnerServiceWrapper(ServiceObject<?> serviceObject) {
            super(serviceObject.interfaceClass());
            this.serviceObject = serviceObject;
        }

        /**
         * @return the serviceObject
         */
        public ServiceObject<?> getServiceObject() {
            return serviceObject;
        }
    }

}
