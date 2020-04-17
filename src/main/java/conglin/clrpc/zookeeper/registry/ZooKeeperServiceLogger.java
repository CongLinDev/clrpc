package conglin.clrpc.zookeeper.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Calculatable;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.registry.ServiceLogger;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.zookeeper.util.ZooKeeperUtils;

public class ZooKeeperServiceLogger extends AbstractZooKeeperService implements ServiceLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceLogger.class);

    private final boolean enable;

    private final Map<String, Calculatable<?>> holder;

    private final Role role;

    public ZooKeeperServiceLogger(Role role, PropertyConfigurer configurer) {
        super("logger", configurer, "address");
        this.role = role;
        enable = configurer.getOrDefault(role.toString() + ".logger.traffic.enable", false);
        if (enable) {
            holder = new HashMap<>();
            init();
        } else {
            holder = Collections.emptyMap();
        }
    }

    /**
     * 初始化
     */
    protected void init() {
        new Timer("zookeeper logger", true).schedule(new TimerTask() {
            @Override
            public void run() {
                holder.entrySet().forEach(entry -> {
                    String data = entry.getValue().toString();
                    ZooKeeperUtils.createNode(keeper, entry.getKey(), data, CreateMode.PERSISTENT_SEQUENTIAL);
                    LOGGER.info("Traffic counts: {}", data);
                });
            }
        }, 1000, 500);
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void put(String key, Calculatable<?> calculatable) {
        holder.put(rootPath + "/traffic/" + role.toString() + "/" + key + "/log", calculatable);
    }

    @Override
    public void remove(String key) {
        holder.remove(rootPath + "/" + key);
    }

}