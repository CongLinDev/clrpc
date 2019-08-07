package conglin.clrpc.transfer.net.message;

public class BasicResponse {
    private Long requestId;
    private String error;
    private Object result;
    // 发送回复的地址
    private String remoteAddress;

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

    public String getRemoteAddress(){
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress){
        this.remoteAddress = remoteAddress;
    }

    public boolean isError() {
        return (error != null);
    }

    @Override
    public String toString() {
        if(isError()){
            return "BasicResponse [requestId=" + requestId + ", error=" + error + "from remote address=" + remoteAddress + "]";
        }else{
            return "BasicResponse [requestId=" + requestId + ", result=" + result + "from remote address=" + remoteAddress + "]";
        }
    }

    public static BasicResponseBuilder builder() {
        return new BasicResponseBuilder(new BasicResponse());
    }

}