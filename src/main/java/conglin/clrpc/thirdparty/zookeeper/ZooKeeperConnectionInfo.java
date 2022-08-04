package conglin.clrpc.thirdparty.zookeeper;

public class ZooKeeperConnectionInfo {
    private final String connectString;
    private int sessionTimeout = 5000;

    private String path = "/clrpc";

    /**
     * @param connectString
     * @param sessionTimeout
     */
    public ZooKeeperConnectionInfo(String connectString) {
        this.connectString = connectString;
    }

    /**
     * @return the connectString
     */
    public String getConnectString() {
        return connectString;
    }

    /**
     * @param sessionTimeout the sessionTimeout to set
     */
    public void setSessionTimeoutValue(String sessionTimeoutValue) {
        if (sessionTimeoutValue != null && !sessionTimeoutValue.isEmpty()) {
            this.sessionTimeout = Integer.parseInt(sessionTimeoutValue);
        }
    }

    /**
     * @param sessionTimeout the sessionTimeout to set
     */
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * @return the sessionTimeout
     */
    public int getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        if (path != null)
            this.path = path;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }
}
