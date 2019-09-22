package conglin.clrpc.common.identifier;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ZooKeeperUtils;
import conglin.clrpc.common.util.atomic.ZooKeeperAtomicService;

public class SequetialIdentifierGenerator extends ZooKeeperAtomicService implements IdentifierGenerator{

    private static final Logger log = LoggerFactory.getLogger(SequetialIdentifierGenerator.class);

    protected BasicIdentifierGenerator downgradeGenerator;

    public SequetialIdentifierGenerator(){
        super("/request/id");
        downgradeGenerator = new BasicIdentifierGenerator();
    }

    @Override
    public Long generateIdentifier() {
        return generateIndentifier(null);
    }

    @Override
    public Long generateIndentifier(String key) {
        if (keeper != null) {
            String nodeSequetialId = ZooKeeperUtils.createNode(keeper, rootPath, CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequetialId.substring(nodeSequetialId.lastIndexOf('/') + 3, nodeSequetialId.length());
            return Long.parseLong(id);
        }
        log.warn("'SequetialIdentifierGenerator' generated Indentifier failed. Starting use 'BasicIdentifierGenerator'.");
        return downgradeGenerator.generateIdentifier(key);
    }

    @Override
    public void close() {
        super.close();
    }

}