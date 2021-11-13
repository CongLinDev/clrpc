package conglin.clrpc.thirdparty.zookeeper.identifier;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.identifier.RandomIdentifierGenerator;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SequentialIdentifierGenerator extends AbstractZooKeeperService implements IdentifierGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequentialIdentifierGenerator.class);

    // 降级ID生成器
    // 本对象不可用时调用
    protected IdentifierGenerator downgradeGenerator;

    public SequentialIdentifierGenerator(Properties properties) {
        super(new UrlScheme(properties.getProperty("extension.atomicity.url")), "id");
        downgradeGenerator = new RandomIdentifierGenerator();
    }

    @Override
    public long generate() {
        return generate(null);
    }

    @Override
    public long generate(String key) {
        if (keeperInstance != null) {
            String nodeSequentialId = ZooKeeperUtils.createNode(keeperInstance.instance(), rootPath, CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequentialId.substring(nodeSequentialId.lastIndexOf('/') + 3, nodeSequentialId.length());
            return Long.parseLong(id);
        }
        LOGGER.warn(
                "'SequentialIdentifierGenerator' generated Identifier failed. Starting use 'BasicIdentifierGenerator'.");
        return downgradeGenerator.generate(key);
    }
}