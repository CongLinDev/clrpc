package conglin.clrpc.test.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.WatcherType;

import conglin.clrpc.common.util.ZooKeeperUtils;

public class ZooKeeperUtilsTest {
    public static void main(String[] args) {
        ZooKeeper keeper = ZooKeeperUtils.connectNewZooKeeper("127.0.0.1:2181", 5000);

        try {
            ZooKeeperUtils.createNode(keeper, "/temp", "hello", CreateMode.EPHEMERAL);
            Watcher watcher = event -> {
                if(event.getType() == Event.EventType.NodeDataChanged){
                    System.out.println("Watcher trigger");
                }
            };
            String s = ZooKeeperUtils.watchNode(keeper, "/temp", watcher);
            System.out.println(s);
            Thread.sleep(1000);
            ZooKeeperUtils.removeAllWatchers(keeper, "/temp", WatcherType.Any);
            ZooKeeperUtils.setNodeData(keeper, "/temp", "null");
            
            ZooKeeperUtils.disconnectZooKeeper(keeper);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}