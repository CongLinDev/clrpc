package conglin.clrpc.thirdparty.zookeeper.executor;

import java.util.Properties;

import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.executor.TransactionRequestExecutor;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.thirdparty.zookeeper.ZooKeeperTransactionHelper;

public class ZooKeeperTransactionRequestExecutor extends TransactionRequestExecutor {
    @Override
    protected TransactionHelper newTransactionHelper() {
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        return ZooKeeperTransactionHelper.getInstance(properties);
    }
}
