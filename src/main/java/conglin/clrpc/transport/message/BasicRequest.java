package conglin.clrpc.transport.message;

import java.util.Arrays;
import java.util.Objects;

public class BasicRequest extends Message {

    private static final long serialVersionUID = 8095197377322231798L;

    transient public static final int MESSAGE_TYPE = 1;

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    private final String serviceName;
    private final String methodName;
    private final Object[] parameters;

    public BasicRequest(BasicRequest request) {
        super(request);
        this.serviceName = request.serviceName();
        this.methodName = request.methodName();
        this.parameters = request.parameters();
    }

    public BasicRequest(Long messageId, String serviceName, String methodName, Object[] parameters) {
        super(messageId);
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    /**
     * 获取服务名
     * 
     * @return the serviceName
     */
    final public String serviceName() {
        return serviceName;
    }

    /**
     * 获取方法名
     * 
     * @return the methodName
     */
    final public String methodName() {
        return methodName;
    }

    /**
     * 获取参数
     * 
     * @return the parameters
     */
    final public Object[] parameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "BasicRequest [messageId=" + messageId() + ", serviceName=" + serviceName + ", methodName="
                + methodName + ", parameters=" + Arrays.toString(parameters) + "]";
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode() ^ methodName.hashCode() ^ Objects.hash(parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof BasicRequest))
            return false;
        BasicRequest r = (BasicRequest) obj;
        return this.serviceName.equals(r.serviceName()) && this.methodName.equals(r.methodName())
                && Objects.deepEquals(parameters, r.parameters());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Object[] p = new Object[parameters.length];
        System.arraycopy(parameters, 0, p, 0, parameters.length);
        return new BasicRequest(messageId(), serviceName, methodName, p);
    }

}
