package conglin.clrpc.common.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * 使用 SnakeYAML 解析 yml配置文件
 * 配置文件名默认为 clrpc-config.yml
 */
public class ConfigParser{

    private static final Logger log = LoggerFactory.getLogger(ConfigParser.class);

    private static String CONFIG_FILENAME = "clrpc-config.yml";

    private Map<String, Object> configs;

    @SuppressWarnings("unchecked")
    private ConfigParser(){
        try(InputStream inputStream = new FileInputStream(CONFIG_FILENAME)){
            Yaml yaml = new Yaml();
            configs = (Map<String, Object>) yaml.load(inputStream);
        }catch(FileNotFoundException e){
            log.error("You must add config file named 'clrpc-config.yml' in your project.");
        }catch(IOException e){
            log.error(e.toString());
        }
    }

    private static class SingletonHolder {
        private static final ConfigParser CONFIG_PARSER = new ConfigParser();
    }

    public static ConfigParser getInstance(){
        return SingletonHolder.CONFIG_PARSER;
    }

    /**
     * 调用该方法前一定要确保
     * 未调用 {@link ConfigParser.getInstance()} 方法
     * 否则该方法将不起任何作用
     * 建议在创建启动类前调用
     * @param path
     */
    public static void setConfigFilePath(String path){
        if(path.endsWith("/")){
            CONFIG_FILENAME = path + CONFIG_FILENAME;
        }else{
            CONFIG_FILENAME = path + "/" + CONFIG_FILENAME;
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T t){
        Object obj = get(key);
        if(obj == null) return t;

        try{
            return (T) t.getClass().cast(obj);
        }catch(ClassCastException exception){
            return t;
        }
    }

    @SuppressWarnings("unchecked")
    public Object get(String key){
        String [] paths = key.split("\\.");
        Object obj = configs;

        try{
            for(int i = 0; i < paths.length; i++){
                obj = ((Map<String, Object>)Map.class.cast(obj)).get(paths[i]);
                if(obj == null) return null;
            }
            return obj;
        }catch(ClassCastException exception){
            return null;
        }
    }
}