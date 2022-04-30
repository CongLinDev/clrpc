package conglin.clrpc.thirdparty.zookeeper.channelhandler;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.channelhandler.AbstractProviderTransactionServiceChannelHandler;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperTransactionHelper;

public class ZooKeeperProviderTransactionServiceChannelHandler extends AbstractProviderTransactionServiceChannelHandler {

    @Override
    protected TransactionHelper newTransactionHelper(UrlScheme urlScheme) {
        return new ZooKeeperTransactionHelper(urlScheme);
    }
}