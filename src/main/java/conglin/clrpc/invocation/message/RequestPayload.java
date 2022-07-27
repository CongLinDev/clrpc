package conglin.clrpc.invocation.message;

import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;

public class RequestPayload implements Payload {

    @Serial
    private static final long serialVersionUID = 8095197377322231798L;

    transient public static final int PAYLOAD_TYPE = 1;

    private final String serviceName;
    private final String methodName;
    private final Object[] parameters;

    /**
     * 构造基本请求对象
     *
     * @param request
     *
     * @see #RequestPayload(String, String, Object[])
     */
    public RequestPayload(RequestPayload request) {
        this.serviceName = request.serviceName();
        this.methodName = request.methodName();
        this.parameters = request.parameters();
    }

    /**
     * 构造基本请求对象
     *
     * @param serviceName 服务名
     * @param methodName  方法名
     * @param parameters  参数
     */
    public RequestPayload(String serviceName, String methodName, Object[] parameters) {
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
        return "RequestPayload{" +
                "serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode() ^ methodName.hashCode() ^ Objects.hash(parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof RequestPayload r))
            return false;
        return this.serviceName.equals(r.serviceName()) && this.methodName.equals(r.methodName())
                && Objects.deepEquals(parameters, r.parameters());
    }
}
