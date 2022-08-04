package conglin.clrpc.invocation.message;

public interface ResponsePayload extends Payload {
    /**
     * 结果
     *
     * @return the result
     */
    Object result();

    /**
     * @return the error
     */
    boolean isError();
}
