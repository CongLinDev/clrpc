package conglin.clrpc.common.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class YamlPropertyConfigurer implements PropertyConfigurer {

    private static final Logger log = LoggerFactory.getLogger(YamlPropertyConfigurer.class);

    protected final Map<String, Object> configs;

    public YamlPropertyConfigurer() {
        this(DEFAULT_CONFIG_FILENAME + ".yml");
    }

    public YamlPropertyConfigurer(String filename) {
        Map<String, Object> tempConfig = null;
        try(InputStream inputStream = new FileInputStream(filename)){
            Yaml yaml = new Yaml();
            tempConfig = (Map<String, Object>)Map.class.cast(yaml.load(inputStream));
        }catch(FileNotFoundException e){
            log.error("You had better add config file named 'clrpc-config.yml' in your project.");
            tempConfig = new HashMap<>();
        }catch(IOException | ClassCastException e){
            log.error(e.getMessage());
            tempConfig = new HashMap<>();
        }finally{
            configs = tempConfig;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object get(String key) {
        String [] paths = key.split("\\.");
        Object obj = configs;

        try{
            for(String path : paths){
                obj = ((Map<String, Object>)Map.class.cast(obj)).get(path);
                if(obj == null) return null;
            }
            return obj;
        }catch(ClassCastException exception){
            return null;
        }
    }

}