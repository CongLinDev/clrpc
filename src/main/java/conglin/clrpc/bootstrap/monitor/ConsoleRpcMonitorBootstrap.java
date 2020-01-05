package conglin.clrpc.bootstrap.monitor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConsoleRpcMonitorBootstrap extends AbstractRpcMonitorBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleRpcMonitorBootstrap.class);

    protected ConsoleRpcMonitorBootstrap() {
        super();
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
    protected void handleConusmer(String serviceName, Map<String, String> nodeAndData) {
        System.out.println("Consumer node Changed. Time=" + System.currentTimeMillis());
        printNodeInfo(serviceName, nodeAndData);
    }

    @Override
    protected void handleProvider(String serviceName, Map<String, String> nodeAndData) {
        System.out.println("Provider node Changed. Time=" + System.currentTimeMillis());
        printNodeInfo(serviceName, nodeAndData);
    }

    /**
     * 打印节点信息
     * 
     * @param serviceName
     * @param nodeAndData
     */
    protected void printNodeInfo(String serviceName, Map<String, String> nodeAndData) {
        System.out.println("Service : " + serviceName);
        nodeAndData.forEach((node, data) -> System.out.println(node + "--->" + data));
    }
}