package conglin.clrpc.bootstrap.monitor;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;

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
    protected void handleConusmer(String serviceName, Collection<Pair<String, String>> nodeList) {
        System.out.println("Consumer node Changed. Time=" + System.currentTimeMillis());
        printNodeInfo(serviceName, nodeList);
    }

    @Override
    protected void handleProvider(String serviceName, Collection<Pair<String, String>> nodeList) {
        System.out.println("Provider node Changed. Time=" + System.currentTimeMillis());
        printNodeInfo(serviceName, nodeList);
    }

    /**
     * 打印节点信息
     * 
     * @param serviceName
     * @param nodeList
     */
    protected void printNodeInfo(String serviceName, Collection<Pair<String, String>> nodeList) {
        System.out.println("Service : " + serviceName);
        nodeList.forEach((node) -> System.out.println(node.getFirst() + "--->" + node.getSecond()));
    }
}