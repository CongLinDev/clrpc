package conglin.clrpc.bootstrap;

import conglin.clrpc.global.role.Role;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 抽象的 RpcBootstrap
 * <p>
 * 用于保存配置对象、控制资源引用计数
 */
abstract public class RpcBootstrap {

    private final Properties properties;

    /**
     * 创建启动对象
     *
     * @param properties 配置
     */
    public RpcBootstrap(Properties properties) {
        if (properties == null) {
            properties = new Properties();
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
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
}