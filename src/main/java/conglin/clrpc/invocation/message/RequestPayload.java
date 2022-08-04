package conglin.clrpc.invocation.message;

public interface RequestPayload extends Payload {

    /**
     * 服务名
     * 
     * @return
     */
    String serviceName();
}
