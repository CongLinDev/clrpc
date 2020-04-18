package conglin.clrpc.common.config;

import java.util.Map;
import java.util.function.Predicate;

public interface PropertyConfigurer {

    String DEFAULT_CONFIG_FILENAME = "clrpc-config";

    /**
     * 获取属性值 若不存在返回null
     * 
     * @param key 键值
     * @return
     */
    Object get(String key);

    /**
     * 获取属性值 若不存在返回null
     * 
     * @param <T>
     * @param key
     * @param clazz
     * @return
     */
    default <T> T get(String key, Class<T> clazz) {
        return (T) clazz.cast(get(key));
    }

    /**
     * 获取属性值 若不存在返回默认值
     * 
     * @param <T>
     * @param key 键值
     * @param t   默认值 not null
     * @return
     */
    @SuppressWarnings("unchecked")
    default <T> T getOrDefault(String key, T t) {
        Object obj = get(key);
        if (obj == null)
            return t;

        try {
            return (T) t.getClass().cast(obj);
        } catch (NullPointerException | ClassCastException exception) {
            return t;
        }
    }

    /**
     * 获取某个键值的值 若满足一定条件,返回搜索值(有可能为默认值) 否则返回默认值
     * 
     * @param <T>
     * @param key
     * @param t
     * @param predicate
     * @return
     */
    default <T> T getOrDefault(String key, T t, Predicate<T> predicate) {
        T value = getOrDefault(key, t);
        if (predicate == null || predicate.test(value)) {
            return value;
        } else {
            return t;
        }
    }

    /**
     * 添加配置项
     * 
     * @param key
     * @param value
     * @return 若配置项已经存在则覆盖并返回原来的值
     */
    Object put(String key, Object value);

    /**
     * 添加配置项
     * 
     * @param key
     * @param value
     * @return 若配置项已经存在则不覆盖并返回原来的值
     */
    default Object putIfAbsent(String key, Object value) {
        Object v = get(key);
        if (v == null) {
            v = put(key, value);
        }
        return v;
    }

    void putAll(Map<String, ? extends Object> map);

    /**
     * 移除配置项
     * 
     * @param key
     * @return
     */
    Object remove(String key);

    /**
     * 清空
     */
    void clear();

    /**
     * 是否是空的配置器
     * 
     * @return
     */
    boolean isEmpty();

    /**
     * 获取子配置器
     * 
     * @param key
     * @return 若key对应的配置器不存在返回空配置器
     */
    PropertyConfigurer subConfigurer(String key);

    /**
     * 获取子配置器
     * 
     * @param specialKey
     * @param commonKey
     * @return 若specialKey对应的配置器不存在，则返回commonKey对应的配置器，若不存在返回空配置器
     */
    PropertyConfigurer subConfigurer(String specialKey, String commonKey);
}