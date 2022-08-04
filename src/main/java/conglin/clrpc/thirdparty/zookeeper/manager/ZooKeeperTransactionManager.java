package conglin.clrpc.thirdparty.zookeeper.manager;

import java.util.Properties;

import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.manager.AbstractTransactionManager;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.thirdparty.zookeeper.ZooKeeperTransactionHelper;

/**
 * 使用 ZooKeeper 控制分布式事务 注意，该类是线程不安全的
 * <p>
 * 在某一时段只能操作一个事务，如果使用者不确定Manager是否可用，可调用 {@link #isAvailable()} 查看
 */
public class ZooKeeperTransactionManager extends AbstractTransactionManager {
    protected TransactionHelper getTransactionHelper() {
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        return ZooKeeperTransactionHelper.getInstance(properties);
    }
}
