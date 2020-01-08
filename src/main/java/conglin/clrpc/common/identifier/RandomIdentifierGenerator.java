package conglin.clrpc.common.identifier;

import java.util.UUID;

/**
 * 使用UUID生成随机的ID
 */
public class RandomIdentifierGenerator implements IdentifierGenerator {

    @Override
    public long generate() {
        return generate(null);
    }

    @Override
    public long generate(String key) {
        return UUID.randomUUID().getLeastSignificantBits();
    }

}