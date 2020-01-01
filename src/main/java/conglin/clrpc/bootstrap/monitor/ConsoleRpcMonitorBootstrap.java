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
    protected void handleConusmer(Map<String, String> nodeAndData) {
        printNodeInfo(nodeAndData);
    }

    @Override
    protected void handleProvider(Map<String, String> nodeAndData) {
        printNodeInfo(nodeAndData);
    }

    /**
     * 打印节点信息
     * @param nodeAndData
     */
    protected void printNodeInfo(Map<String, String> nodeAndData) {
        nodeAndData.forEach((node, data) -> System.out.println(node + "--->" + data));
    }
}