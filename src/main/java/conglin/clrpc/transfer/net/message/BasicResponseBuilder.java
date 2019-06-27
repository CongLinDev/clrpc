package conglin.clrpc.transfer.net.message;

import conglin.clrpc.common.AbstractBuilder;

public final class BasicResponseBuilder extends AbstractBuilder<BasicResponse>{

    BasicResponseBuilder(BasicResponse product) {
        super(product);
    }

    public BasicResponseBuilder requestId(String requestId){
        super.getProduct().setRequestId(requestId);
        return this;
    }

    public BasicResponseBuilder error(String error){
        super.getProduct().setError(error);
        return this;
    }

    public BasicResponseBuilder result(Object result){
        super.getProduct().setResult(result);
        return this;
    }
}