package conglin.clrpc.zookeeper.identifier;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Url;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.identifier.RandomIdentifierGenerator;
import conglin.clrpc.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.zookeeper.util.ZooKeeperUtils;

public class SequetialIdentifierGenerator extends AbstractZooKeeperService implements IdentifierGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequetialIdentifierGenerator.class);

    // 降级ID生成器
    // 本对象不可用时调用
    protected IdentifierGenerator downgradeGenerator;

    public SequetialIdentifierGenerator(PropertyConfigurer configurer) {
        super(new Url(configurer.getOrDefault("atomicity", "zookeeper://127.0.0.1:2181/clrpc?session-timeout=5000")), "id");
        downgradeGenerator = new RandomIdentifierGenerator();
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
        LOGGER.warn(
                "'SequetialIdentifierGenerator' generated Indentifier failed. Starting use 'BasicIdentifierGenerator'.");
        return downgradeGenerator.generate(key);
    }
}