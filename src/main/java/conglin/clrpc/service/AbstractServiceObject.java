package conglin.clrpc.service;

import java.util.Map;

abstract public class AbstractServiceObject<T> implements ServiceObject<T> {

    protected final Map<String, String> metaInfo;

    public AbstractServiceObject(String name, Map<String, String> metaInfo) {
        this.metaInfo = metaInfo;
        metaInfo.putIfAbsent(ServiceObject.SERVICE_NAME, name);
        metaInfo.putIfAbsent(ServiceObject.VERSION, ServiceVersion.defaultVersion().toString());
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
