package conglin.clrpc.transfer.net.message;

import conglin.clrpc.common.AbstractBuilder;

public final class BasicRequestBuilder extends AbstractBuilder<BasicRequest> {

    BasicRequestBuilder(BasicRequest product) {
        super(product);
    }

    public BasicRequestBuilder requestId(String requestId){
        super.getProduct().setRequestId(requestId);
        return this;
    }

	public BasicRequestBuilder className(String className){
        super.getProduct().setClassName(className);
        return this;
    }

    public BasicRequestBuilder methodName(String methodName){
        super.getProduct().setMethodName(methodName);
        return this;
    }

    public BasicRequestBuilder parameterTypes(Class<?>[] parameterTypes){
        super.getProduct().setParameterTypes(parameterTypes);
        return this;
    }

    public BasicRequestBuilder parameters(Object[] parameters){
        super.getProduct().setParameters(parameters);
        return this;
    }
}