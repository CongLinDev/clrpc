package conglin.clrpc.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

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

    private final static Map<String, Object> configs;

    static{
        Map<String, Object> tempConfig = null;
        try(InputStream inputStream = new FileInputStream(CONFIG_FILENAME)){
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

    /**
     * 获取某个键值的值
     * 若不存在返回null
     * @param key 键值
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object get(String key){
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

    /**
     * 获取某个键值的值
     * 若不存在返回默认值
     * @param <T> 
     * @param key 键值
     * @param t 默认值
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(String key, T t){
        Object obj = get(key);
        if(obj == null) return t;

        try{
            return (T) t.getClass().cast(obj);
        }catch(ClassCastException exception){
            return t;
        }
    }

    /**
     * 获取某个键值的值
     * 若满足一定条件,返回搜索值(有可能为默认值)
     * 否则返回默认值
     * @param <T>
     * @param key
     * @param t
     * @param predicate
     * @return
     */
    public static <T> T getOrDefault(String key, T t, Predicate<T> predicate){
        T value = getOrDefault(key, t);
        if(predicate == null) return value;
        
        if(predicate.test(value)){
            return value;
        }else{
            return t;
        }
    }

}