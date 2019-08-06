package conglin.clrpc.transfer.net.message;

import conglin.clrpc.common.AbstractBuilder;

public final class BasicRequestBuilder extends AbstractBuilder<BasicRequest> {

    BasicRequestBuilder(BasicRequest product) {
        super(product);
    }

    public BasicRequestBuilder requestId(Long requestId){
        product.setRequestId(requestId);
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

    public BasicRequestBuilder serviceName(String serviceName){
        product.setServiceName(serviceName);
        return this;
    }
}