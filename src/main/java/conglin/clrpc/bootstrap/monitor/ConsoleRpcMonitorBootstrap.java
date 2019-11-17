package conglin.clrpc.bootstrap.monitor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConsoleRpcMonitorBootstrap extends AbstractRpcMonitorBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleRpcMonitorBootstrap.class);

    protected ConsoleRpcMonitorBootstrap(){
        super();
    }

    @Override
    public void start() throws InterruptedException {
        LOGGER.info("Console monitor started.");
        Thread.sleep(Integer.MAX_VALUE);
    }

    @Override
    public void stop() throws InterruptedException {
        super.stop();
        LOGGER.info("Console monitor stoped.");
    }

    @Override
    protected void handleNodeInfo(Map<String, String> nodeAndData) {
        nodeAndData.forEach( (node, data) -> System.out.println(node + "--->" + data));
    }

}