package conglin.clrpc.bootstrap.monitor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleRpcMonitorBootstrap extends AbstractRpcMonitorBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ConsoleRpcMonitorBootstrap.class);

    protected ConsoleRpcMonitorBootstrap(){
        super();
    }

    @Override
    public void start() throws InterruptedException {
        log.info("Console monitor started.");
        Thread.sleep(Integer.MAX_VALUE);
    }

    @Override
    public void stop() throws InterruptedException {
        super.stop();
        log.info("Console monitor stoped.");
    }

    @Override
    protected void handleNodeInfo(Map<String, String> nodeAndData) {
        nodeAndData.forEach( (node, data) -> System.out.println(node + "--->" + data));
    }

}