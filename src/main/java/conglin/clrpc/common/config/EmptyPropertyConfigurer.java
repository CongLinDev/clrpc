package conglin.clrpc.common.config;

import java.util.Map;

public final class EmptyPropertyConfigurer implements PropertyConfigurer {

    /**
     * 一个空的不可变的属性配置器
     * 
     * @return
     */
    public static EmptyPropertyConfigurer empty() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final EmptyPropertyConfigurer INSTANCE = new EmptyPropertyConfigurer();
    }

    private EmptyPropertyConfigurer() {

    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public Object put(String key, Object value) {
        return null;
    }

    @Override
    public void putAll(Map<String, ? extends Object> map) {
    }

    @Override
    public Object remove(String key) {
        return null;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public PropertyConfigurer subConfigurer(String key) {
        return this;
    }

    @Override
    public PropertyConfigurer subConfigurer(String specialKey, String commonKey) {
        return this;
    }
}