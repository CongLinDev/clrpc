package conglin.clrpc.transfer.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

public class BasicResponseSender implements ResponseSender {

    private static final Logger log = LoggerFactory.getLogger(BasicRequestSender.class);
    
    @Override
    public void sendResponse(Channel channel, BasicResponse response) {
        if(response == null) return;
        channel.writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        log.debug("Sending response for request id=" + response.getRequestId());
    }

    @Override
	public void destory() {
		// do nothing
	}

}