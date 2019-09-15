package conglin.clrpc.common.identifier;

import java.util.UUID;

public class BasicIdentifierGenerator implements IdentifierGenerator {

    @Override
    public Long generateIdentifier() {
        return generateIndentifier(null);
    }

    @Override
    public Long generateIndentifier(String key) {    
        return UUID.randomUUID().getLeastSignificantBits();
    }

    @Override
    public void close() {
        // do nothing
    }
}