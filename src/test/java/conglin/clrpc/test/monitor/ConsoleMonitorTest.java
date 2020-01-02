package conglin.clrpc.test.monitor;

import conglin.clrpc.bootstrap.RpcMonitorBootstrap;
import conglin.clrpc.bootstrap.monitor.RpcMonitorBootstrapFactory;

public class ConsoleMonitorTest {
    public static void main(String[] args) {
        RpcMonitorBootstrap bootstrap = RpcMonitorBootstrapFactory.rpcMonitorBootstrap();

        bootstrap.monitor().monitorService().start();
        bootstrap.stop();

    }
}