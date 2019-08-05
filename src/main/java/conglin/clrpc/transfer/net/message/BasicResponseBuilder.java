package conglin.clrpc.transfer.net.message;

import conglin.clrpc.common.AbstractBuilder;

public final class BasicResponseBuilder extends AbstractBuilder<BasicResponse>{

    BasicResponseBuilder(BasicResponse product) {
        super(product);
    }

    public BasicResponseBuilder requestId(Long requestId){
        product.setRequestId(requestId);
        return this;
    }

    public BasicResponseBuilder error(String error){
        product.setError(error);
        return this;
    }

    public BasicResponseBuilder result(Object result){
        product.setResult(result);
        return this;
    }
}