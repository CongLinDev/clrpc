package conglin.clrpc.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import conglin.clrpc.common.Role;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.lifecycle.ComponentContext;
import conglin.clrpc.lifecycle.ObjectLifecycleUtils;

/**
 * 抽象的 Bootstrap
 * 
 * 用于保存配置对象、控制资源引用计数
 */
abstract public class Bootstrap {

    private static final String DEFAULT_CONFIG_FILE_NAME = "config.properties";

    private final Properties properties;

    /**
     * 创建启动对象
     *
     * @param properties 配置
     */
    public Bootstrap(Properties properties) {
        if (properties == null) {
            properties = new Properties();
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(DEFAULT_CONFIG_FILE_NAME)) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        this.properties = properties;
    }

    /**
     * 返回配置器
     *
     * @return
     */
    protected Properties properties() {
        return properties;
    }

    /**
     * 虚拟机钩子
     *
     * @param runnable
     */
    public void hook(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }

    /**
     * 角色
     *
     * @return
     */
    abstract public Role role();

    /**
     * 返回一个自定义对象，并对对象进行初始化
     * 
     * 该方法用于扩展功能
     *
     * @param clazz 必须提供一个无参构造函数
     * @return
     */
    protected Object object(Class<?> clazz) {
        Object object = ClassUtils.loadObject(clazz);
        ObjectLifecycleUtils.assemble(object, componentContext());
        return object;
    }

    /**
     * 获取 {@link ComponentContext}
     * 
     * @return
     */
    abstract protected ComponentContext componentContext();
}