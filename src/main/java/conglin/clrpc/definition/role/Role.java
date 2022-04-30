package conglin.clrpc.definition.role;

/**
 * rpc的角色
 */
public enum Role {
    CONSUMER("consumer"), PROVIDER("provider"), MONITOR("monitor"), UNKNOWN("unknown");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }

    /**
     * 是否是 Consumer
     * 
     * @return
     */
    public boolean isConsumer() {
        return this == CONSUMER;
    }

    /**
     * 是否是 Provider
     * 
     * @return
     */
    public boolean isProvider() {
        return this == PROVIDER;
    }
}