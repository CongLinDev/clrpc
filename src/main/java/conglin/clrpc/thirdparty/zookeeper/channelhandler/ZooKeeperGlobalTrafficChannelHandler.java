package conglin.clrpc.thirdparty.zookeeper.channelhandler;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.traffic.channel.GlobalTrafficChannelHandler;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.thirdparty.zookeeper.registry.ZooKeeperServiceLogger;

import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class ZooKeeperGlobalTrafficChannelHandler extends GlobalTrafficChannelHandler {

    @Override
    protected void init() {
        PropertyConfigurer c = getContext().getWith(RpcContextEnum.PROPERTY_CONFIGURER);
        String urlString = c.get("extension.logger", String.class);
        serviceLogger = new ZooKeeperServiceLogger(new UrlScheme(urlString));
    }
}
