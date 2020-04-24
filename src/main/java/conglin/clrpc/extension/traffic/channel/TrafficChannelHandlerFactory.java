package conglin.clrpc.extension.traffic.channel;

import java.util.Arrays;
import java.util.Collection;

import conglin.clrpc.service.context.channel.CommonChannelContext;
import conglin.clrpc.service.handler.factory.ChannelHandlerFactory;
import io.netty.channel.ChannelHandler;

/**
 * 将该类的全限定类名
 * {@code conglin.clrpc.extension.traffic.channel.TrafficChannelHandlerFactory}
 * 加入配置文件即可
 */
public class TrafficChannelHandlerFactory implements ChannelHandlerFactory {

    private final CommonChannelContext context;

    public TrafficChannelHandlerFactory(CommonChannelContext context) {
        this.context = context;
    }

    @Override
    public Collection<ChannelHandler> beforeCodec() {
        return Arrays.asList( new GlobalTrafficChannelHandler(context));
    }

}