package conglin.clrpc.transfer.message;

import java.util.Arrays;

public class BasicRequest {
    private Long requestId;
    private String serviceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "BasicRequest [requestId=" + requestId + ", serviceName=" + serviceName
                + ", methodName=" + methodName + ", parameters=" + Arrays.toString(parameters) + ", parameterTypes="
                + Arrays.toString(parameterTypes) + "]";
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode() ^ methodName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(! (obj instanceof BasicRequest)) return false;
        BasicRequest r = (BasicRequest)obj;
        return this.serviceName.equals(r.getServiceName()) &&
                this.methodName.equals(r.getMethodName());
    }

}
