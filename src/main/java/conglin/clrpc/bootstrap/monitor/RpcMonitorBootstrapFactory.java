package conglin.clrpc.bootstrap.monitor;

import conglin.clrpc.bootstrap.RpcMonitorBootstrap;

public class RpcMonitorBootstrapFactory {

    public enum MonitorType{
        CONSOLE
    }

    public static RpcMonitorBootstrap getRpcMonitorBootstrap(MonitorType monitorType){
        switch(monitorType){
            case CONSOLE:
                return new ConsoleRpcMonitorBootstrap();
            default:
                return new ConsoleRpcMonitorBootstrap();
        }
    }
}