package conglin.clrpc.thirdparty.zookeeper.identifier;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;
import org.apache.zookeeper.CreateMode;

public class SpecialSequentialIdentifierGenerator extends AbstractZooKeeperService implements IdentifierGenerator {

    public SpecialSequentialIdentifierGenerator(UrlScheme urlScheme) {
        super(urlScheme, "id");
    }

    @Override
    public long generate() {
        return generate("Anonymous");
    }

    @Override
    public long generate(String key) {
        String sequentialNode = buildPath(key);
        String nodeSequentialId = ZooKeeperUtils.createNode(keeperInstance.instance(), sequentialNode, "",
                CreateMode.EPHEMERAL_SEQUENTIAL);
        String id = nodeSequentialId.substring(nodeSequentialId.lastIndexOf('/') + 3, nodeSequentialId.length());
        return Long.parseLong(id);
    }

}