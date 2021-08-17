package conglin.clrpc.thirdparty.zookeeper.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Calculable;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.registry.ServiceLogger;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;

/**
 * 日志记录
 * 
 * 地址默认为 {@code /clrpc/traffic/}
 */
public class ZooKeeperServiceLogger extends AbstractZooKeeperService implements ServiceLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceLogger.class);

    private final Map<String, Calculable<?>> holder;

    public ZooKeeperServiceLogger(UrlScheme url) {
        super(url, "traffic");
        holder = new HashMap<>();
        init(1000, Integer.parseInt(url.getParameter("period")));
    }

    /**
     * 初始化
     * 
     * @param delay
     * @param period
     */
    protected void init(long delay, long period) {
        final Timer timer = new Timer("zookeeper logger", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Entry<String, Calculable<?>> entry : holder.entrySet()) {
                    String data = entry.getValue().calculate().toString();
                    if (keeperInstance.instance().getState().isAlive()) {
                        ZooKeeperUtils.createNode(keeperInstance.instance(), entry.getKey(), data, CreateMode.PERSISTENT_SEQUENTIAL);
                        LOGGER.info("Traffic(key={}) counts: {}", entry.getKey(), data);
                    } else {
                        timer.cancel();
                        break;
                    }
                }
            }
        }, delay, period);
    }

    @Override
    public void put(String key, Calculable<?> calculable) {
        holder.put(rootPath + "/" + key + "/log", calculable);
    }

    @Override
    public void remove(String key) {
        holder.remove(rootPath + "/" + key);
    }
}