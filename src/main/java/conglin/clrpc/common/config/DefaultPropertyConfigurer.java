package conglin.clrpc.common.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPropertyConfigurer implements PropertyConfigurer {

    private static final Logger log = LoggerFactory.getLogger(DefaultPropertyConfigurer.class);

    private final Properties properties;

    public DefaultPropertyConfigurer() {
        this(DEFAULT_CONFIG_FILENAME + ".properties");
    }

    public DefaultPropertyConfigurer(String filename) {
        properties = new Properties();
        try(InputStream inputStream = new FileInputStream(filename)){
            properties.load(inputStream);
        }catch(FileNotFoundException e){
            log.error(e.getMessage());
        }catch(IOException e){
            log.error(e.getMessage());
        }
    }

    @Override
    public Object get(String key) {
        return properties.getProperty(key);
    }
}