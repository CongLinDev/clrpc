package conglin.clrpc.global.role;

/**
 * rpc的角色
 */
public enum Role {
    CONSUMER("consumer"), PROVIDER("provider"), UNKNOWN("unknown");

    private String role;

    Role(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }

    /**
     * 补全配置项
     * 
     * @param suffix
     * @return
     */
    public String item(String suffix) {
        return role + suffix;
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