package conglin.clrpc.common.config;

import java.io.IOException;
import java.io.InputStream;

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
     * 将默认文件内容导入配置器
     * 
     * @return
     */
    public static JsonPropertyConfigurer fromFile() {
        return fromFile(DEFAULT_CONFIG_FILENAME + ".json");
    }

    /**
     * 将文件内容导入配置器
     * 
     * @param filename
     * @return
     */
    public static JsonPropertyConfigurer fromFile(String filename) {
        return new JsonPropertyConfigurer(true, filename);
    }

    /**
     * 将字符串内容导入配置器
     * 
     * @param content 配置内容
     * @return
     */
    public static JsonPropertyConfigurer fromContent(String content) {
        return new JsonPropertyConfigurer(false, content);
    }

    /**
     * 获取一个空的配置器
     */
    public JsonPropertyConfigurer() {
        this(false, "{}");
    }

    /**
     * 获取一个配置器
     * 
     * @param fromFile 是否是从文件读取。若是，则解析文件内容。若不是，直接解析内容。
     * @param content
     */
    protected JsonPropertyConfigurer(boolean fromFile, String content) {
        if (fromFile) {
            CONFIG_HOLDER = JSONObject.parseObject(new String(resolveFile(content)));
        } else {
            CONFIG_HOLDER = JSONObject.parseObject(content);
        }
    }

    /**
     * 解析文件
     * 
     * @param filename 文件名
     * @return 文件字节数组
     */
    protected static byte[] resolveFile(String filename) {
        try (InputStream inputStream = PropertyConfigurer.class.getClassLoader().getResourceAsStream(filename)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            LOGGER.error("Resolve File [" + filename + "] failed. " + e.getMessage());
            return "{}".getBytes();
        }
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