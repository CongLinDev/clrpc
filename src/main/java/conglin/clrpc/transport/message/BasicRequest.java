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

    public BasicRequest(Long requestId) {
        super(requestId);
    }

    public BasicRequest(BasicRequest request) {
        super(request);
        this.serviceName = request.getServiceName();
        this.methodName = request.getMethodName();
        this.parameterTypes = request.getParameterTypes();
        this.parameters = request.getParameters();
    }

    /**
     * 获取服务名
     * 
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 设置服务名
     * 
     * @param serviceName the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * 获取方法名
     * 
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 设置方法名
     * 
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * 获取参数类型
     * 
     * @return the parameterTypes
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * 设置参数类型
     * 
     * @param parameterTypes the parameterTypes to set
     */
    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * 获取参数
     * 
     * @return the parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * 设置参数
     * 
     * @param parameters the parameters to set
     */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "BasicRequest [requestId=" + getRequestId() + ", serviceName=" + serviceName + ", methodName="
                + methodName + ", parameters=" + Arrays.toString(parameters) + ", parameterTypes="
                + Arrays.toString(parameterTypes) + "]";
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        BasicRequest r = new BasicRequest(getRequestId());
        r.setServiceName(serviceName);
        r.setMethodName(methodName);

        Class<?>[] pt = new Class<?>[parameterTypes.length];
        System.arraycopy(parameterTypes, 0, pt, 0, parameterTypes.length);
        r.setParameterTypes(pt);

        Object[] p = new Object[parameters.length];
        System.arraycopy(parameters, 0, p, 0, parameters.length);
        r.setParameters(p);

        return r;
    }

}
