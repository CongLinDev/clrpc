package conglin.clrpc.common.exception;

public class NoSuchServiceException extends RuntimeException{

    private static final long serialVersionUID = -2704737758397975272L;
    
    private String requestId;
    private String className;
    private String methodName;

    public NoSuchServiceException(String requestId, String className, String methodName){
        this.requestId = requestId;
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "NoSuchServiceException [className=" + className + ", methodName=" + methodName + ", requestId="
                + requestId + "]";
    }

    @Override
    public String getMessage() {
        return "There is no available service for your request. Details: " + toString();
    }

}