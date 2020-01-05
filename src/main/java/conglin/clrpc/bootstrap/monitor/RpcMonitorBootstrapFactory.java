package conglin.clrpc.bootstrap.monitor;

import conglin.clrpc.bootstrap.RpcMonitorBootstrap;

public class RpcMonitorBootstrapFactory {

    /**
     * 监视器类型
     */
    public enum MonitorType {
        CONSOLE
    }

    /**
     * 获取监视器启动器
     * 
     * @param monitorType
     * @return
     */
    public static RpcMonitorBootstrap rpcMonitorBootstrap(MonitorType monitorType) {
        switch (monitorType) {
        case CONSOLE:
            return new ConsoleRpcMonitorBootstrap();
        default:
            return new ConsoleRpcMonitorBootstrap();
        }
    }

    /**
     * 获取监视器启动器 默认为控制台类型的监视器启动器
     * 
     * @return
     */
    public static RpcMonitorBootstrap rpcMonitorBootstrap() {
        return rpcMonitorBootstrap(MonitorType.CONSOLE);
    }
}