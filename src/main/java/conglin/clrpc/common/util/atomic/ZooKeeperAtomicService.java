package conglin.clrpc.common.util.atomic;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.util.ZooKeeperUtils;

abstract public class ZooKeeperAtomicService implements Destroyable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperAtomicService.class);

    protected final ZooKeeper keeper;
    protected final String rootPath;

    public ZooKeeperAtomicService(PropertyConfigurer configurer){
        this(configurer, "/unnamed");
    }

    
    public ZooKeeperAtomicService(PropertyConfigurer configurer, String subPath){
        this(configurer.getOrDefault("zookeeper.atomicity.address", "127.0.0.1:2181"),
            configurer.getOrDefault("zookeeper.session.timeout", 5000),
            configurer.getOrDefault("zookeeper.atomicity.root-path", "/clrpc"),
            subPath);
    }

    public ZooKeeperAtomicService(String atomicityAddress, int sessionTimeout, String mainPath, String subPath){
        rootPath = mainPath.endsWith("/") ? mainPath + "atomic" + subPath : mainPath + "/atomic" + subPath;
        keeper = ZooKeeperUtils.connectZooKeeper(atomicityAddress, sessionTimeout);
    }

    @Override
    public void destroy() throws DestroyFailedException {
        try{
            ZooKeeperUtils.disconnectZooKeeper(keeper);
            LOGGER.debug("ZooKeeper AtomicService shuted down.");
        }catch(InterruptedException e){
            throw new DestroyFailedException(e.getMessage());
        }
    }

    @Override
    public boolean isDestroyed() {
        return !keeper.getState().isAlive();
    }
}