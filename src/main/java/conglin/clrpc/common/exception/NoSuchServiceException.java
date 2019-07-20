package conglin.clrpc.common.exception;

public class NoSuchServiceException extends RuntimeException{

    private static final long serialVersionUID = -2704737758397975272L;
    
    private String requestId;
    private String serviceName;
    private String methodName;

    public NoSuchServiceException(String requestId, String serviceName, String methodName){
        this.requestId = requestId;
        this.serviceName = serviceName;
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "NoSuchServiceException [serviceName=" + serviceName + ", methodName=" + methodName + ", requestId="
                + requestId + "]";
    }

    @Override
    public String getMessage() {
        return "There is no available service for your request. Details: " + toString();
    }

}