package conglin.clrpc.transport.message;

import java.io.Serializable;

public record Message(Long messageId, Payload payload) implements Serializable {
}
