package conglin.clrpc.test.monitor;

import conglin.clrpc.bootstrap.RpcMonitorBootstrap;
import conglin.clrpc.bootstrap.monitor.RpcMonitorBootstrapFactory;
import conglin.clrpc.bootstrap.monitor.RpcMonitorBootstrapFactory.MonitorType;

public class ConsoleMonitorTest {
    public static void main(String[] args) {
        RpcMonitorBootstrap bootstrap = RpcMonitorBootstrapFactory.getRpcMonitorBootstrap(MonitorType.CONSOLE);

        bootstrap.monitor().monitorService().start();
        bootstrap.stop();

    }
}