package conglin.clrpc.transfer.net.receiver;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.net.message.BasicResponse;

public class BasicResponseReceiver implements ResponseReceiver {

    private ClientServiceHandler serviceHandler;

    @Override
    public void init(ClientServiceHandler serviceHandler) {
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
	public void stop() {
		// do nothing
	}

    @Override
    public ExecutorService getExecutorService() {
        // if(serviceHandler == null) return null;
        return serviceHandler.getExecutorService();
    }
}