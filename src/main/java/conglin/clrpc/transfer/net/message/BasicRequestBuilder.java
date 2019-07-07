package conglin.clrpc.transfer.net.message;

import conglin.clrpc.common.AbstractBuilder;

public final class BasicRequestBuilder extends AbstractBuilder<BasicRequest> {

    BasicRequestBuilder(BasicRequest product) {
        super(product);
    }

    public BasicRequestBuilder requestId(String requestId){
        product.setRequestId(requestId);
        return this;
    }

	public BasicRequestBuilder className(String className){
        product.setClassName(className);
        return this;
    }

    public BasicRequestBuilder methodName(String methodName){
        product.setMethodName(methodName);
        return this;
    }

    public BasicRequestBuilder parameterTypes(Class<?>[] parameterTypes){
        product.setParameterTypes(parameterTypes);
        return this;
    }

    public BasicRequestBuilder parameters(Object[] parameters){
        product.setParameters(parameters);
        return this;
    }
}