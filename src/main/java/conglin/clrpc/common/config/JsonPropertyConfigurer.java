package conglin.clrpc.common.config;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonPropertyConfigurer implements PropertyConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPropertyConfigurer.class);

    private final JSONObject CONFIG_HOLDER;

    public JsonPropertyConfigurer() {
        this(DEFAULT_CONFIG_FILENAME + ".json");
    }

    public JsonPropertyConfigurer(String filename) {
        this(resolveFile(filename));
    }

    public JsonPropertyConfigurer(byte[] configContent) {
        CONFIG_HOLDER = JSONObject.parseObject(new String(configContent));
    }

    private static byte[] resolveFile(String filename) {
        try (InputStream inputStream = PropertyConfigurer.class.getClassLoader().getResourceAsStream(filename)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            LOGGER.error("Resolve File [" + filename + "] failed. " + e.getMessage());
            return new byte[0];
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