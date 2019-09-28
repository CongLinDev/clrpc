package conglin.clrpc.common.identifier;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ZooKeeperUtils;

public class SpecialSequetialIdentifierGenerator extends SequetialIdentifierGenerator {

    private static final Logger log = LoggerFactory.getLogger(SpecialSequetialIdentifierGenerator.class);

    @Override
    public long generate() {
        return generate("Anonymous");
    }

    @Override
    public long generate(String key) {
        if (super.keeper != null) {
            String sequetialNode = rootPath + "/service/" + key + "/request/id";
            String nodeSequetialId = ZooKeeperUtils.createNode(keeper, sequetialNode, "", CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequetialId.substring(nodeSequetialId.lastIndexOf('/') + 3, nodeSequetialId.length());
            return Long.parseLong(id);
        }
        log.warn("'SpecialSequetialIdentifierGenerator' generated Indentifier failed. Starting use 'SequetialIdentifierGenerator'.");
        return super.generate(key);
    }

    @Override
    public void close() {
        super.close();
    }
}