package conglin.clrpc.common.identifier;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ConfigParser;
import conglin.clrpc.common.util.ZooKeeperUtils;

public class SequetialIdentifierGenerator extends BasicIdentifierGenerator{

    private static final Logger log = LoggerFactory.getLogger(SequetialIdentifierGenerator.class);

    protected final String rootPath; //zookeeper根地址
    protected final ZooKeeper keeper;

    public SequetialIdentifierGenerator(){
        String registryAddress = ConfigParser.getOrDefault("zookeeper.atomicity.address", "localhost:2181");
        int sessionTimeout = ConfigParser.getOrDefault("zookeeper.session.timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);

        String path = ConfigParser.getOrDefault("zookeeper.registry.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path.substring(0, path.length()-1) : path;//去除最后一个 /
    }

    @Override
    public Long generateIdentifier() {
        return generateIndentifier(null);
    }

    @Override
    public Long generateIndentifier(String key) {
        if (keeper != null) {
            String sequetialNode = rootPath + "/request/id";
            String nodeSequetialId = ZooKeeperUtils.createNode(keeper, sequetialNode, "", CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequetialId.substring(nodeSequetialId.lastIndexOf('/') + 3, nodeSequetialId.length());
            return Long.parseLong(id);
        }
        log.warn("'SequetialIdentifierGenerator' generated Indentifier failed. Starting use 'BasicIdentifierGenerator'.");
        return super.generateIndentifier(key);
    }

    @Override
    public void close() {
        try{
            ZooKeeperUtils.disconnectZooKeeper(keeper);
            log.debug("Sequetial request sender shuted down.");
        }catch(InterruptedException e){
            log.error(e.getMessage());
        }
    }

}