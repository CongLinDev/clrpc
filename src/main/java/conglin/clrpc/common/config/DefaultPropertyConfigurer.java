package conglin.clrpc.common.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated(since = "0.7.7")
public class DefaultPropertyConfigurer implements PropertyConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPropertyConfigurer.class);

    private final Properties PROPERTIES;

    public DefaultPropertyConfigurer() {
        this(DEFAULT_CONFIG_FILENAME + ".properties");
    }

    public DefaultPropertyConfigurer(String filename) {
        PROPERTIES = new Properties();
        try(InputStream inputStream = new FileInputStream(filename)){
            PROPERTIES.load(inputStream);
        }catch(FileNotFoundException e){
            LOGGER.error(e.getMessage());
        }catch(IOException e){
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public String get(String key) {
        return PROPERTIES.getProperty(key);
    }
}