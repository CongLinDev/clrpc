package conglin.clrpc.thirdparty.zookeeper.identifier;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;

public class SpecialSequentialIdentifierGenerator extends SequentialIdentifierGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecialSequentialIdentifierGenerator.class);

    public SpecialSequentialIdentifierGenerator(PropertyConfigurer configurer){
        super(configurer);
    }

    @Override
    public long generate() {
        return generate("Anonymous");
    }

    @Override
    public long generate(String key) {
        if (keeperInstance != null) {
            String sequentialNode = rootPath + "/" + key;
            String nodeSequentialId = ZooKeeperUtils.createNode(keeperInstance.instance(), sequentialNode, "", CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequentialId.substring(nodeSequentialId.lastIndexOf('/') + 3, nodeSequentialId.length());
            return Long.parseLong(id);
        }
        LOGGER.warn("'SpecialSequentialIdentifierGenerator' generated Identifier failed. Starting use 'SequentialIdentifierGenerator'.");
        return super.generate(key);
    }
    
}