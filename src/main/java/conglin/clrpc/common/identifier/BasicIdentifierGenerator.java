package conglin.clrpc.common.identifier;

import java.util.UUID;

import javax.security.auth.DestroyFailedException;

public class BasicIdentifierGenerator implements IdentifierGenerator {

    @Override
    public long generate() {
        return generate(null);
    }

    @Override
    public long generate(String key) {    
        return UUID.randomUUID().getLeastSignificantBits();
    }

    @Override
    public void destroy() throws DestroyFailedException {
        // do nothing
    }
}