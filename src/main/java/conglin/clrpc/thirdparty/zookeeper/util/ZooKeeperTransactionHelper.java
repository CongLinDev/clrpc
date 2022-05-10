package conglin.clrpc.thirdparty.zookeeper.util;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.transaction.TransactionException;
import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.TransactionState;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.WatcherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooKeeperTransactionHelper extends AbstractZooKeeperService implements TransactionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperTransactionHelper.class);

    private static final String STATE_SEPARATOR = "#";

    private static String buildValue(String... values) {
        if (values == null || values.length == 0) return "";
        if (values.length == 1) return values[0];
        return String.join(STATE_SEPARATOR, values);
    }

    public ZooKeeperTransactionHelper(UrlScheme url) {
        super(url, "transaction");
    }

    @Override
    public void begin(String path, String target) throws TransactionException {
        String nodePath = buildPath(path);
        // 创建事务根节点，并将节点值设为 TransactionState.PREPARE
        if (ZooKeeperUtils.createNode(keeperInstance.instance(), nodePath, buildValue(TransactionState.PREPARE.name(), target)) == null)
            throw new TransactionException("Transaction begin failed. (path = " + nodePath + ")");
    }

    @Override
    public void prepare(String path, String target) throws TransactionException {
        String nodePath = buildPath(path);
        // 检查是否已经存在节点
        String existState = ZooKeeperUtils.getNodeData(keeperInstance.instance(), nodePath);
        if (existState != null) {
            // 存在节点 查看节点状态
            if (existState.startsWith(buildValue(TransactionState.PREPARE.name()))) {
                // 覆盖值
                ZooKeeperUtils.compareAndSetNodeData(keeperInstance.instance(), nodePath, existState, buildValue(TransactionState.PREPARE.name(), target));
            } else {
                throw new TransactionException("prepare fail " + existState);
            }
        } else {
            // 不存在节点
            // 创建临时子节点，并将节点值设为 TransactionState.PREPARE.name(), target
            ZooKeeperUtils.createNode(keeperInstance.instance(), nodePath, buildValue(TransactionState.PREPARE.name(), target));
        }
    }

    @Override
    public boolean isOccupied(String path, String target) throws TransactionException {
        String nodePath = buildPath(path);
        String data = ZooKeeperUtils.getNodeData(keeperInstance.instance(), nodePath);
        return buildValue(TransactionState.PREPARE.name(), target).equals(data);
    }

    @Override
    public boolean signPrecommit(String path, String target) throws TransactionException {
        String oldValue = buildValue(TransactionState.PREPARE.name(), target);
        String newValue = buildValue(TransactionState.PRECOMMIT.name(), target);
        return ZooKeeperUtils.compareAndSetNodeData(keeperInstance.instance(), buildPath(path), oldValue, newValue);
    }

    @Override
    public boolean signAbort(String path, String target) throws TransactionException {
        String oldValue = buildValue(TransactionState.PREPARE.name(), target);
        String newValue = buildValue(TransactionState.ABORT.name(), target);
        return ZooKeeperUtils.compareAndSetNodeData(keeperInstance.instance(), buildPath(path), oldValue, newValue);
    }

    @Override
    public boolean signCommit(String path, String target) throws TransactionException {
        String oldValue = buildValue(TransactionState.PRECOMMIT.name(), target);
        String newValue = buildValue(TransactionState.COMMIT.name(), target);
        return ZooKeeperUtils.compareAndSetNodeData(keeperInstance.instance(), buildPath(path), oldValue, newValue);
    }

    @Override
    public void watch(String path, Callback callback) throws TransactionException {
        String nodePath = buildPath(path);
        String prepareStatePrefix = buildValue(TransactionState.PREPARE.name());
        String commitStatePrefix = buildValue(TransactionState.COMMIT.name());
        String abortStatePrefix = buildValue(TransactionState.ABORT.name());
        Watcher watcher = event -> {
            String newState = ZooKeeperUtils.watchNode(keeperInstance.instance(), nodePath, null);
            if (Event.EventType.NodeDataChanged == event.getType()) {       // 节点被修改，说明状态改变
                if (newState.startsWith(commitStatePrefix)) {
                    LOGGER.debug("Transaction state is COMMIT and execute success(). path={} value={}", path, newState);
                    callback.success(null);
                } else if (newState.startsWith(abortStatePrefix)) {
                    LOGGER.debug("Transaction state is ABORT and execute fail(). path={} value={}", path, newState);
                    callback.fail(null);
                } else if (newState.startsWith(prepareStatePrefix)) {
                    LOGGER.error("Transaction state is PREPARE. The coordinator may be choose another one. And execute fail(). path={} value={}", path, newState);
                    callback.fail(null);
                } else {
                    LOGGER.error("Unknown transaction state, and execute fail(). path={} value={}", path, newState);
                    callback.fail(null);
                }
            } else if (Event.EventType.NodeDeleted == event.getType()) {     // 节点被删除，说明协调者离开集群了，自动回滚
                LOGGER.error("Transaction node has been deleted. The coordinator maybe has some problems. path={}", path);
                callback.fail(null);
            }
        };

        String currentState = ZooKeeperUtils.watchNode(keeperInstance.instance(), nodePath, watcher);

        if (currentState == null) {
            LOGGER.debug("Transaction state is null and execute fail(). path={}", path);
            ZooKeeperUtils.removeWatcher(keeperInstance.instance(), nodePath, watcher, WatcherType.Data);
            callback.fail(null);
            return;
        }

        if (currentState.startsWith(prepareStatePrefix)) // 正常情况下当前状态应当为 TransactionState.PREPARE
            return;

        // 请求已经被处理了
        ZooKeeperUtils.removeWatcher(keeperInstance.instance(), nodePath, watcher, WatcherType.Data);
        if (currentState.startsWith(commitStatePrefix)) { // 请求状态已经更改为 COMMIT
            LOGGER.debug("Transaction state is COMMIT and execute success(). path={}", path);
            callback.success(null);
        } else if (currentState.startsWith(abortStatePrefix)) { // 请求状态已经更改为 ABORT
            LOGGER.debug("Transaction state is ABORT and execute fail(). path={}", path);
            callback.fail(null);
        } else {
            LOGGER.error("Unknown transaction state, and execute fail(). path={} state={}", path, currentState);
            callback.fail(null);
        }
    }

    @Override
    public void abort(String path, String target) throws TransactionException {
        String transactionPath = buildPath(path);
        // 将 transactionPath 节点标记为 TransactionState.ABORT
        if (ZooKeeperUtils.setNodeData(keeperInstance.instance(), transactionPath, buildValue(TransactionState.ABORT.name(), target)) == null) {
            throw new TransactionException("abort transaction failed. path=" + transactionPath);
        }
    }

    @Override
    public void commit(String path, String target) throws TransactionException {
        String transactionPath = buildPath(path);
        // 将 transactionPath 节点标记为 TransactionState.COMMIT
        if (ZooKeeperUtils.setNodeData(keeperInstance.instance(), transactionPath, buildValue(TransactionState.COMMIT.name(), target)) == null) {
            throw new TransactionException("abort transaction failed. path=" + transactionPath);
        }
    }
}