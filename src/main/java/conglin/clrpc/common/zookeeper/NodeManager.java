package conglin.clrpc.common.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeManager {

    private static final Logger log = LoggerFactory.getLogger(NodeManager.class);

    /**
     * 递归创建通用的持久节点
     * 该节点不存储任何信息
     * 采用 OPEN_ACL_UNSAFE 策略
     * @param keeper 
     * @param path 绝对路径
     * @param data
     */
    public static void createNode(ZooKeeper keeper, String path, String data){
        createNode(keeper, path, data, CreateMode.PERSISTENT);
    }
    
    /**
     * 递归创建通用的节点
     * 该节点不存储任何信息
     * 采用 OPEN_ACL_UNSAFE 策略
     * @param keeper 
     * @param path 绝对路径
     * @param data
     * @param mode 节点类型
     */
    public static void createNode(ZooKeeper keeper, String path, String data, CreateMode mode) {
        //创建上级节点
        int index = path.lastIndexOf("/");
        createNode(keeper, path.substring(0, index));

        try {
            Stat stat = keeper.exists(path, false);
            if(stat == null){
                String subPath = keeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
                log.debug("create zookeeper node: " + subPath);
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("", e);
        }
    }

    /**
     * 递归创建通用的持久节点
     * 该节点不存储任何信息
     * 采用 OPEN_ACL_UNSAFE 策略
     * @param keeper
     * @param path 绝对路径
     */
    public static void createNode(ZooKeeper keeper, String path){
        if("".equals(path)) return;
        try{
            Stat stat = keeper.exists(path, false);
            if(stat == null){
                int index = path.lastIndexOf("/");
                String higherLevelPath = path.substring(0, index);
                createNode(keeper, higherLevelPath);
                String subPath = keeper.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.debug("create zookeeper node:" + subPath);
            }
        }catch(KeeperException | InterruptedException e){
            log.error(e.getMessage());
        }
    }
}