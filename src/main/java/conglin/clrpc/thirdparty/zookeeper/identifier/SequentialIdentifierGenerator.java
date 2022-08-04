package conglin.clrpc.thirdparty.zookeeper.identifier;

import conglin.clrpc.invocation.identifier.IdentifierGenerator;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.thirdparty.zookeeper.ZooKeeperConnectionInfo;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;

public class SequentialIdentifierGenerator extends AbstractZooKeeperService implements IdentifierGenerator {

    public SequentialIdentifierGenerator(ZooKeeperConnectionInfo connectionInfo) {
        super(connectionInfo, "id");
    }

    @Override
    public long generate() {
        return generate(null);
    }

    @Override
    public long generate(String key) {
        String nodeSequentialId = ZooKeeperUtils.createEphemeralSequentialNode(keeperInstance.instance(), rootPath, "");
        String id = nodeSequentialId.substring(nodeSequentialId.lastIndexOf('/') + 3, nodeSequentialId.length());
        return Long.parseLong(id);
    }
}