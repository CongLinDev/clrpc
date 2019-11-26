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
        this(PropertyConfigurer.class.getClassLoader().getResourceAsStream(filename));
    }

    public JsonPropertyConfigurer(InputStream inputStream){
        String configString = "";
        try {
            configString = new String(inputStream.readAllBytes());
            inputStream.close(); // 关闭流
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            CONFIG_HOLDER = JSONObject.parseObject(configString);
        }
    }

    @Override
    public Object get(String key) {
        return CONFIG_HOLDER.get(key);
    }

    @Override
    public Object put(String key, Object value){
        return CONFIG_HOLDER.put(key, value);
    }
}