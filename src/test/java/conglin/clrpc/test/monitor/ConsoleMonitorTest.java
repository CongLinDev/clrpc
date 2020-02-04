package conglin.clrpc.test.monitor;

import conglin.clrpc.bootstrap.RpcMonitorBootstrap;
import conglin.clrpc.bootstrap.monitor.ConsoleRpcMonitorBootstrap;

public class ConsoleMonitorTest {
    public static void main(String[] args) {
        RpcMonitorBootstrap bootstrap = new ConsoleRpcMonitorBootstrap();

        bootstrap.listServices().forEach(System.out::println);

        bootstrap.monitor().start();
        bootstrap.stop();

    }
}