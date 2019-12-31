package conglin.clrpc.transport.message;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class BasicRequest extends Message implements Serializable {

    private static final long serialVersionUID = 8095197377322231798L;

    transient public static final int MESSAGE_TYPE = 2;

    private String serviceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    public BasicRequest(Long requestId){
        super(requestId);
    }

    /**
     * 服务名
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @return the parameterTypes
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * @param parameterTypes the parameterTypes to set
     */
    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * @return the parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "BasicRequest [requestId=" + getRequestId() + ", serviceName=" + serviceName + ", methodName=" + methodName
                + ", parameters=" + Arrays.toString(parameters) + ", parameterTypes=" + Arrays.toString(parameterTypes)
                + "]";
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode() ^ methodName.hashCode() ^ Objects.hash((Object[]) parameterTypes)
                ^ Objects.hash(parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof BasicRequest))
            return false;
        BasicRequest r = (BasicRequest) obj;
        return this.serviceName.equals(r.getServiceName()) && this.methodName.equals(r.getMethodName())
                && Objects.deepEquals(parameterTypes, r.getParameterTypes())
                && Objects.deepEquals(parameters, r.getParameters());
    }

}
