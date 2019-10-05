package conglin.clrpc.common.util.atomic;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ConfigParser;
import conglin.clrpc.common.util.ZooKeeperUtils;

abstract public class ZooKeeperAtomicService {
    private static final Logger log = LoggerFactory.getLogger(ZooKeeperAtomicService.class);

    protected final ZooKeeper keeper;
    protected final String rootPath;

    public ZooKeeperAtomicService(){
        this("/unnamed");
    }

    public ZooKeeperAtomicService(String subPath){
        this(ConfigParser.getOrDefault("zookeeper.atomicity.root-path", "/clrpc"), subPath);
    }

    public ZooKeeperAtomicService(String mainPath, String subPath){
        this(ConfigParser.getOrDefault("zookeeper.atomicity.address", "localhost:2181"), mainPath, subPath);
    }

    public ZooKeeperAtomicService(String atomicityAddress, String mainPath, String subPath){
        this(atomicityAddress, ConfigParser.getOrDefault("zookeeper.session.timeout", 5000), mainPath, subPath);
    }

    public ZooKeeperAtomicService(String atomicityAddress, int sessionTimeout, String mainPath, String subPath){
        rootPath = mainPath.endsWith("/") ? mainPath + "atomic" + subPath : mainPath + "/atomic" + subPath;
        keeper = ZooKeeperUtils.connectZooKeeper(atomicityAddress, sessionTimeout);
    }

    public void destroy(){
        try{
            ZooKeeperUtils.disconnectZooKeeper(keeper);
            log.debug("ZooKeeper AtomicService shuted down.");
        }catch(InterruptedException e){
            log.error(e.getMessage());
        }
    }
}