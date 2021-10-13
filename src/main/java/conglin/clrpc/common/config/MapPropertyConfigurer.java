package conglin.clrpc.common.config;

import java.util.HashMap;
import java.util.Map;

public class MapPropertyConfigurer implements PropertyConfigurer {

    private final Map<String, Object> container;

    public MapPropertyConfigurer() {
        container = new HashMap<>();
    }

    public MapPropertyConfigurer(Map<String, ?> map) {
        container = new HashMap<>(map);
    }

    @Override
    public Object get(String key) {
        return container.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return container.put(key, value);
    }

    @Override
    public void putAll(Map<String, ?> map) {
        container.putAll(map);
    }

    @Override
    public Object remove(String key) {
        return container.remove(key);
    }

    @Override
    public void clear() {
        container.clear();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public String toString() {
        if (container.isEmpty()) return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : container.entrySet()) {
            stringBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue().toString())
                        .append('\n');
        }
        return stringBuilder.toString();
    }
}
