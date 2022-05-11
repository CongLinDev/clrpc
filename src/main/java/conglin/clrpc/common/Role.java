package conglin.clrpc.common;

/**
 * 角色
 */
public enum Role {
    CONSUMER, PROVIDER, MONITOR, UNKNOWN;

    /**
     * 是否是 Consumer
     * 
     * @return
     */
    public boolean isConsumer() {
        return this.equals(CONSUMER);
    }

    /**
     * 是否是 Provider
     * 
     * @return
     */
    public boolean isProvider() {
        return this.equals(PROVIDER);
    }
}