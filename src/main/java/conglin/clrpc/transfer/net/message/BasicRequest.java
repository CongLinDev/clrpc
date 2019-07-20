package conglin.clrpc.transfer.net.message;

import java.util.Arrays;

public class BasicRequest {
    private String requestId;
    private String serviceName;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    public static BasicRequestBuilder builder(){
        return new BasicRequestBuilder(new BasicRequest());
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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
        return "BasicRequest [className=" + className + ", methodName=" + methodName + ", parameterTypes="
                + Arrays.toString(parameterTypes) + ", parameters=" + Arrays.toString(parameters) + ", requestId="
                + requestId + ", serviceName=" + serviceName + "]";
    }

}
