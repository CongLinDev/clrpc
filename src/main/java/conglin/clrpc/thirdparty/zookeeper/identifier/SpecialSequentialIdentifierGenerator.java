package conglin.clrpc.thirdparty.zookeeper.identifier;

import conglin.clrpc.invocation.identifier.IdentifierGenerator;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.thirdparty.zookeeper.ZooKeeperConnectionInfo;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;

public class SpecialSequentialIdentifierGenerator extends AbstractZooKeeperService implements IdentifierGenerator {

    public SpecialSequentialIdentifierGenerator(ZooKeeperConnectionInfo connectionInfo) {
        super(connectionInfo, "id");
    }

    @Override
    public long generate() {
        return generate("Anonymous");
    }

    @Override
    public long generate(String key) {
        String sequentialNode = buildPath(key);
        String nodeSequentialId = ZooKeeperUtils.createEphemeralSequentialNode(keeperInstance.instance(), sequentialNode, "");
        String id = nodeSequentialId.substring(nodeSequentialId.lastIndexOf('/') + 3, nodeSequentialId.length());
        return Long.parseLong(id);
    }

}