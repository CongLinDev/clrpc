package conglin.clrpc.common.identifier;

import javax.security.auth.DestroyFailedException;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;
import conglin.clrpc.common.util.atomic.ZooKeeperAtomicService;

public class SequetialIdentifierGenerator extends ZooKeeperAtomicService implements IdentifierGenerator {

    private static final Logger log = LoggerFactory.getLogger(SequetialIdentifierGenerator.class);

    protected BasicIdentifierGenerator downgradeGenerator;

    public SequetialIdentifierGenerator(PropertyConfigurer configurer){
        super(configurer, "/request/id");
        downgradeGenerator = new BasicIdentifierGenerator();
    }

    @Override
    public long generate() {
        return generate(null);
    }

    @Override
    public long generate(String key) {
        if (keeper != null) {
            String nodeSequetialId = ZooKeeperUtils.createNode(keeper, rootPath, CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequetialId.substring(nodeSequetialId.lastIndexOf('/') + 3, nodeSequetialId.length());
            return Long.parseLong(id);
        }
        log.warn("'SequetialIdentifierGenerator' generated Indentifier failed. Starting use 'BasicIdentifierGenerator'.");
        return downgradeGenerator.generate(key);
    }

    @Override
    public void destroy() throws DestroyFailedException {
        super.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return super.isDestroyed();
    }
}