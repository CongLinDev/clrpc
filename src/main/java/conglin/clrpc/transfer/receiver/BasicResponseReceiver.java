package conglin.clrpc.transfer.receiver;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicResponse;

public class BasicResponseReceiver implements ResponseReceiver {

    protected ConsumerServiceHandler serviceHandler;

    @Override
    public void init(ConsumerServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public void handleResponse(BasicResponse response) {
        Long requestId = response.getRequestId();
        //直接移除
        RpcFuture future = serviceHandler.removeFuture(requestId);

        if(future != null){
            future.done(response);
        }
    }

    @Override
	public void destory() {
		// do nothing
	}

    @Override
    public ExecutorService getExecutorService() {
        return serviceHandler.getExecutorService();
    }
}