package conglin.clrpc.service;

import java.util.Map;

abstract public class AbstractServiceObject<T> implements ServiceObject<T> {
    protected final Map<String, String> metaInfo;
    protected final Class<T> interfaceClass;

    public AbstractServiceObject(Class<T> interfaceClass, Map<String, String> metaInfo) {
        this(interfaceClass.getName(), interfaceClass, metaInfo);
    }

    public AbstractServiceObject(String name, Class<T> interfaceClass, Map<String, String> metaInfo) {
        this.metaInfo = metaInfo;
        this.interfaceClass = interfaceClass;
        metaInfo.put(ServiceObject.INTERFACE, interfaceClass.getName());
        metaInfo.putIfAbsent(ServiceObject.SERVICE_NAME, name);
        metaInfo.putIfAbsent(ServiceObject.VERSION, ServiceVersion.defaultVersion().toString());
    }

    @Override
    public Class<T> interfaceClass() {
        return interfaceClass;
    }

    @Override
    public String name() {
        return metaInfo.get(ServiceObject.SERVICE_NAME);
    }

    @Override
    public Map<String, String> metaInfo() {
        return metaInfo;
    }

    @Override
    public ServiceVersion version() {
        return ServiceVersion.parse(metaInfo.get(ServiceObject.VERSION));
    }

}
