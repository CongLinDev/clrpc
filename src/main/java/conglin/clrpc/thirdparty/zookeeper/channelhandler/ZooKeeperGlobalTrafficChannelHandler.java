package conglin.clrpc.thirdparty.zookeeper.channelhandler;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.traffic.channel.GlobalTrafficChannelHandler;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.thirdparty.zookeeper.registry.ZooKeeperServiceLogger;
import io.netty.channel.ChannelHandler.Sharable;

import java.util.Properties;

@Sharable
public class ZooKeeperGlobalTrafficChannelHandler extends GlobalTrafficChannelHandler {

    @Override
    public void init() {
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        String urlString = properties.getProperty("extension.logger.url");
        serviceLogger = new ZooKeeperServiceLogger(new UrlScheme(urlString));
    }
}
