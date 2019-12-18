package conglin.clrpc.global;

import conglin.clrpc.common.util.ZooKeeperUtils;

/**
 * clrpc 为了性能选择复用部分资源，但是这些资源在对象使用完后无法知晓是否后续会被在用
 * 所以使用全局资源管理器在JVM关闭前关闭该资源
 */
public class GlobalResourceManager {
    /**
     * 销毁资源
     */
    public static void destroy(){
        ZooKeeperUtils.disconnectAllZooKeeper();
    }
}