package conglin.clrpc.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
        return new JsonPropertyConfigurer(Source.FILE, filename);
    }

    /**
     * 将网络文件内容导入配置器
     * 
     * @param url
     * @return
     */
    public static JsonPropertyConfigurer fromURL(String url) {
        return new JsonPropertyConfigurer(Source.NETWORK, url);
    }

    /**
     * 将字符串内容导入配置器
     * 
     * @param content 配置内容
     * @return
     */
    public static JsonPropertyConfigurer fromContent(String content) {
        return new JsonPropertyConfigurer(Source.CONTENT, content);
    }

    /**
     * 获取一个空的配置器
     */
    public JsonPropertyConfigurer() {
        this(Source.CONTENT, emptyJsonString());
    }

    /**
     * 获取一个配置器
     * 
     * @param source
     * @param content
     */
    public JsonPropertyConfigurer(Source source, String content) {
        switch (source) {
        case CONTENT: // 输入应当是json数据
            CONFIG_HOLDER = JSONObject.parseObject(content);
            break;
        case FILE: // 输入应当是文件名
            CONFIG_HOLDER = JSONObject.parseObject(new String(resolveFile(content)));
            break;
        case NETWORK:
            CONFIG_HOLDER = JSONObject.parseObject(new String(resolveURL(content)));
            break;
        default: // 默认返回一个空的配置器
            CONFIG_HOLDER = JSONObject.parseObject(emptyJsonString());
        }
    }

    /**
     * 解析本地文件
     * 
     * @param filename 文件名
     * @return 文件字节数组
     */
    protected static byte[] resolveFile(String filename) {
        try (InputStream inputStream = PropertyConfigurer.class.getClassLoader().getResourceAsStream(filename)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            LOGGER.error("Resolve File={} failed. Cause: {}", filename, e);
            return emptyJsonBytes();
        }
    }

    /**
     * 解析网络文件
     * 
     * @param url
     * @return
     */
    protected static byte[] resolveURL(String url) {
        try (InputStream inputStream = new URL(url).openConnection().getInputStream()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            LOGGER.error("Resolve URL={} failed. Cause: {}", url, e);
            return emptyJsonBytes();
        }
    }

    /**
     * 空的json对象字符串
     * 
     * @return
     */
    public static String emptyJsonString() {
        return "{}";
    }

    /**
     * 空的json对象字节数组
     * 
     * @return
     */
    public static byte[] emptyJsonBytes() {
        return emptyJsonString().getBytes();
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
    public String toString() {
        return CONFIG_HOLDER.toJSONString();
    }
}