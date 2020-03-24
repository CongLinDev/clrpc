package conglin.clrpc.zookeeper.identifier;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.zookeeper.util.ZooKeeperUtils;

public class SpecialSequetialIdentifierGenerator extends SequetialIdentifierGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecialSequetialIdentifierGenerator.class);

    public SpecialSequetialIdentifierGenerator(PropertyConfigurer configurer){
        super(configurer);
    }

    @Override
    public long generate() {
        return generate("Anonymous");
    }

    @Override
    public long generate(String key) {
        if (super.keeper != null) {
            String sequetialNode = rootPath + key + "/request/id";
            String nodeSequetialId = ZooKeeperUtils.createNode(keeper, sequetialNode, "", CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequetialId.substring(nodeSequetialId.lastIndexOf('/') + 3, nodeSequetialId.length());
            return Long.parseLong(id);
        }
        LOGGER.warn("'SpecialSequetialIdentifierGenerator' generated Indentifier failed. Starting use 'SequetialIdentifierGenerator'.");
        return super.generate(key);
    }
    
}