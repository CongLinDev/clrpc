package conglin.clrpc.thirdparty.zookeeper.executor;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.executor.TransactionRequestExecutor;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperTransactionHelper;

public class ZooKeeperTransactionRequestExecutor extends TransactionRequestExecutor {
    @Override
    protected TransactionHelper newTransactionHelper(UrlScheme urlScheme) {
        return new ZooKeeperTransactionHelper(urlScheme);
    }
}
