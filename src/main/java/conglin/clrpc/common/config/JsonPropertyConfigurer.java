package conglin.clrpc.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonPropertyConfigurer implements PropertyConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPropertyConfigurer.class);

    private final JSONObject CONFIG_HOLDER;

    /**
     * 返回一个空的配置器
     * 
     * @return
     */
    public static JsonPropertyConfigurer empty() {
        return new JsonPropertyConfigurer();
    }

    /**
     * 将默认本地文件内容导入配置器
     * 
     * @return
     */
    public static JsonPropertyConfigurer fromFile() {
        return fromFile(DEFAULT_CONFIG_FILENAME + ".json");
    }

    /**
     * 将本地文件内容导入配置器
     * 
     * @param filename
     * @return
     */
    public static JsonPropertyConfigurer fromFile(String filename) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            return fromContent(new String(inputStream.readAllBytes()));
        } catch (IOException e) {
            LOGGER.error("Resolve File={} failed. Cause: {}", filename, e);
            return empty();
        }
    }

    /**
     * 将本地文件内容导入配置器
     * 
     * @param file
     * @return
     */
    public static JsonPropertyConfigurer fromFile(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return fromContent(new String(inputStream.readAllBytes()));
        } catch (IOException e) {
            LOGGER.error("Resolve File={} failed. Cause: {}", file.getName(), e);
            return empty();
        }
    }

    /**
     * 将网络文件内容导入配置器
     * 
     * @param url
     * @return
     */
    public static JsonPropertyConfigurer fromURL(URL url) {
        try (InputStream inputStream = url.openConnection().getInputStream()) {
            return fromContent(new String(inputStream.readAllBytes()));
        } catch (IOException e) {
            LOGGER.error("Resolve URL={} failed. Cause: {}", url, e);
            return empty();
        }
    }

    /**
     * 将字符串内容导入配置器
     * 
     * @param content 配置内容
     * @return
     */
    public static JsonPropertyConfigurer fromContent(String content) {
        return new JsonPropertyConfigurer(content);
    }

    /**
     * 将 {@link java.util.Map} 内元素作为配置器
     * 
     * @param map 配置内容
     * @return
     */
    @SuppressWarnings("unchecked")
    public static JsonPropertyConfigurer fromMap(Map<String, ? extends Object> map) {
        return new JsonPropertyConfigurer((Map<String, Object>) map);
    }

    /**
     * 获取一个空的配置器
     */
    protected JsonPropertyConfigurer() {
        this(new JSONObject());
    }

    /**
     * 使用 {@link java.util.Map} 初始化配置器
     * 
     * @param map
     */
    protected JsonPropertyConfigurer(Map<String, Object> map) {
        this(new JSONObject(map));
    }

    /**
     * 使用文本内容初始化配置器
     * 
     * @param content
     */
    protected JsonPropertyConfigurer(String content) {
        this(JSONObject.parseObject(content));
    }

    /**
     * 使用json对象直接获取配置器
     * 
     * @param jsonObject
     */
    protected JsonPropertyConfigurer(JSONObject jsonObject) {
        this.CONFIG_HOLDER = jsonObject;
    }

    @Override
    public Object get(String key) {
        return CONFIG_HOLDER.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return CONFIG_HOLDER.put(key, value);
    }

    @Override
    public void putAll(Map<String, ? extends Object> map) {
        CONFIG_HOLDER.putAll(map);
    }

    @Override
    public void clear() {
        CONFIG_HOLDER.clear();
    }

    @Override
    public boolean isEmpty() {
        return CONFIG_HOLDER.isEmpty();
    }

    @Override
    public Object remove(String key) {
        return CONFIG_HOLDER.remove(key);
    }

    @Override
    public PropertyConfigurer subConfigurer(String key) {
        JSONObject object = CONFIG_HOLDER.getJSONObject(key);
        // if cannot find subconfigurer, will return empty configurer
        if (object == null)
            object = new JSONObject();
        return new JsonPropertyConfigurer(object);
    }

    @Override
    public PropertyConfigurer subConfigurer(String specialKey, String commonKey) {
        JSONObject object = CONFIG_HOLDER.getJSONObject(specialKey);
        if (object == null) {
            return subConfigurer(commonKey);
        } else {
            return new JsonPropertyConfigurer(object);
        }
    }

    @Override
    public String toString() {
        return CONFIG_HOLDER.toJSONString();
    }
}