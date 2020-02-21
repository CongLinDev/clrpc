package conglin.clrpc.bootstrap.monitor;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.config.PropertyConfigurer;

public class ConsoleRpcMonitorBootstrap extends AbstractRpcMonitorBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleRpcMonitorBootstrap.class);

    public ConsoleRpcMonitorBootstrap() {
        this(null);
    }

    public ConsoleRpcMonitorBootstrap(PropertyConfigurer configurer) {
        super(configurer);
    }

    @Override
    public void start() {
        super.start();

        LOGGER.info("Console monitor started.");
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Console monitor stoped.");
        super.stop();
    }

    @Override
    protected void handleConsumer(String serviceName, Collection<Pair<String, String>> nodeList) {
        System.out.println(serviceName + " Consumer node Changed. Time=" + System.currentTimeMillis());
        printNodeInfo(nodeList);
    }

    @Override
    protected void handleProvider(String serviceName, Collection<Pair<String, String>> nodeList) {
        System.out.println(serviceName + " Provider node Changed. Time=" + System.currentTimeMillis());
        printNodeInfo(nodeList);
    }

    /**
     * 打印节点信息
     * 
     * @param serviceName
     * @param nodeList
     */
    protected void printNodeInfo(Collection<Pair<String, String>> nodeList) {
        if(!nodeList.isEmpty()) {
            nodeList.forEach(node -> System.out.println("\t" + node.getFirst() + "--->" + node.getSecond()));
        } else {
            System.out.println("\tEmpty list.");
        }
    }
}