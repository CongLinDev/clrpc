package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.serialization.SerializationHandler;

public class RpcMonitorOption extends RpcCommonOption {

    /**
     * 设置序列化处理器
     * 
     * @param serializationHandler the serializationHandler to set
     * @return this
     */
    public RpcMonitorOption setSerializationHandler(SerializationHandler serializationHandler) {
        super.serializationHandler(serializationHandler);
        return this;
    }

    // 监视地址（ZooKeeper地址）
    protected String monitorAddress;

    /**
     * 返回监视地址（ZooKeeper地址）
     * 
     * @return the monitorAddress
     */
    public String getMonitorAddress() {
        return monitorAddress;
    }

    /**
     * 设置监视地址（ZooKeeper地址）
     * 
     * @param monitorAddress the monitorAddress to set
     * @return this
     */
    public RpcMonitorOption setMonitorAddress(String monitorAddress) {
        this.monitorAddress = monitorAddress;
        return this;
    }

    // 监视路径（ZooKeeper路径）
    protected String monitorPath;

    /**
     * 返回监视路径（ZooKeeper路径）
     * 
     * @return the monitorPath
     */
    public String getMonitorPath() {
        return monitorPath;
    }

    /**
     * 设置监视路径（ZooKeeper路径）
     * 
     * @param monitorPath the monitorPath to set
     * @return this
     */
    public RpcMonitorOption setMonitorPath(String monitorPath) {
        this.monitorPath = monitorPath;
        return this;
    }
}