package conglin.clrpc.transfer.net.message;

public class BasicResponse {
    private Long requestId;
    private String error;
    private Object result;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isError() {
        return (error != null);
    }

    @Override
    public String toString() {
        return "BasicResponse [error=" + error + ", requestId=" + requestId + ", result=" + result + "]";
    }

    public static BasicResponseBuilder builder() {
        return new BasicResponseBuilder(new BasicResponse());
    }

}