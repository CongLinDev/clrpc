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

import java.util.Collection;

public class ZooKeeperTransactionHelper extends AbstractZooKeeperService implements TransactionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperTransactionHelper.class);

    private static final String SEPARATOR = "#";

    private static String buildValue(String... values) {
        if (values == null || values.length == 0) return "";
        if (values.length == 1) return values[0];
        StringBuilder stringBuilder = new StringBuilder();
        for (String v : values) {
            stringBuilder.append(v).append(SEPARATOR);
        }
        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }

    public ZooKeeperTransactionHelper(UrlScheme url) {
        super(url, "transaction");
    }

    @Override
    public void begin(String path) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        // 创建事务根节点，并将节点值设为 TransactionState.PREPARE
        if (ZooKeeperUtils.createNode(keeperInstance.instance(), nodePath, buildValue(TransactionState.PREPARE.name())) == null)
            throw new TransactionException("Transaction begin failed. (path = " + nodePath + ")");
    }

    @Override
    public void prepare(String path, String target) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        // 创建临时子节点，并将节点值设为 TransactionState.PREPARE.name(), target
        if (ZooKeeperUtils.createNode(keeperInstance.instance(), nodePath, buildValue(TransactionState.PREPARE.name(), target)) == null)
            throw new TransactionException("Transaction execute failed. (sub_path = " + nodePath + " )");
    }

    @Override
    public boolean isOccupied(String path, String target) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        String data = ZooKeeperUtils.getNodeData(keeperInstance.instance(), nodePath);
        return buildValue(TransactionState.PREPARE.name(), target).equals(data);
    }

    @Override
    public boolean signSuccess(String path, String target) throws TransactionException {
        String oldValue = buildValue(TransactionState.PREPARE.name(), target);
        String newValue = buildValue(TransactionState.PRECOMMIT.name());
        return ZooKeeperUtils.compareAndSetNodeData(keeperInstance.instance(), rootPath + "/" + path, oldValue, newValue);
    }

    @Override
    public boolean signFailed(String path, String target) throws TransactionException {
        String oldValue = buildValue(TransactionState.PREPARE.name(), target);
        String newValue = buildValue(TransactionState.ABORT.name());
        return ZooKeeperUtils.compareAndSetNodeData(keeperInstance.instance(), rootPath + "/" + path, oldValue, newValue);
    }

    @Override
    public void watch(String path, Callback callback) throws TransactionException {
        String subNodePath = rootPath + "/" + path;
        String preCommitState = buildValue(TransactionState.PRECOMMIT.name());
        String commitState = buildValue(TransactionState.COMMIT.name());
        String abortState = buildValue(TransactionState.ABORT.name());
        Watcher watcher = event -> {
            String newState = ZooKeeperUtils.watchNode(keeperInstance.instance(), subNodePath, null);
            if (Event.EventType.NodeDataChanged == event.getType()) {       // 节点被修改，说明状态改变
                if (commitState.equals(newState)) {
                    LOGGER.debug("Transaction state is {} and execute success(). path={}", commitState, path);
                    callback.success(null);
                } else if (abortState.equals(newState)) {
                    LOGGER.debug("Transaction state is {} and execute fail(). path={}", abortState, path);
                    callback.fail(null);
                } else {
                    LOGGER.error("Unknown transaction state, and execute fail(). path={} state={}", path, newState);
                    callback.fail(null);
                }
            } else if (Event.EventType.NodeDeleted == event.getType()) {     // 节点被删除，说明协调者离开集群了，自动回滚
                LOGGER.error("Transaction node has been deleted. Coordinator maybe has some problems. path={}", path);
                callback.fail(null);
            }
        };

        String currentState = ZooKeeperUtils.watchNode(keeperInstance.instance(), subNodePath, watcher);

        if (currentState == null) {
            LOGGER.debug("Transaction state is null and execute fail(). path={}", path);
            callback.fail(null);
            return;
        }

        if (preCommitState.equals(currentState)) // 正常情况下当前状态应当为 TransactionState.PRECOMMIT
            return;

        // 请求已经被处理了
        if (commitState.equals(currentState)) { // 请求状态已经更改为 COMMIT
            ZooKeeperUtils.removeWatcher(keeperInstance.instance(), subNodePath, watcher, WatcherType.Data);
            LOGGER.debug("Transaction state is {} and execute success(). path={}", commitState, path);
            callback.success(null);
        } else if (abortState.equals(currentState)) { // 请求状态已经更改为 ABORT
            ZooKeeperUtils.removeWatcher(keeperInstance.instance(), subNodePath, watcher, WatcherType.Data);
            LOGGER.debug("Transaction state is {} and execute fail(). path={}", abortState, path);
            callback.fail(null);
        } else {
            LOGGER.error("Unknown transaction state, and execute fail(). path={} state={}", path, currentState);
            callback.fail(null);
        }
    }

    @Override
    public void abort(String path) throws TransactionException {
        String transactionPath = rootPath + "/" + path;
        // 将 transactionPath 节点及其子节点全部标记为 TransactionState.ABORT
        if (ZooKeeperUtils.setNodeData(keeperInstance.instance(), transactionPath, buildValue(TransactionState.ABORT.name())) == null) {
            throw new TransactionException("abort transaction failed. path=" + transactionPath);
        }

        Collection<String> subNodes = ZooKeeperUtils.listChildrenNode(keeperInstance.instance(), transactionPath, null);
        for (String subNode : subNodes) {
            String subNodePath = transactionPath + "/" + subNode;
            if (ZooKeeperUtils.setNodeData(keeperInstance.instance(), subNodePath, buildValue(TransactionState.ABORT.name())) == null) {
                throw new TransactionException("abort transaction failed. path=" + subNodePath);
            }
        }
    }

    @Override
    public void commit(String path) throws TransactionException {
        String transactionPath = rootPath + "/" + path;
        // 将 transactionPath 节点及其子节点全部标记为 TransactionState.COMMIT
        if (ZooKeeperUtils.setNodeData(keeperInstance.instance(), transactionPath, buildValue(TransactionState.COMMIT.name())) == null) {
            throw new TransactionException("abort transaction failed. path=" + transactionPath);
        }

        Collection<String> subNodes = ZooKeeperUtils.listChildrenNode(keeperInstance.instance(), transactionPath, null);
        for (String subNode : subNodes) {
            String subNodePath = transactionPath + "/" + subNode;
            if (ZooKeeperUtils.setNodeData(keeperInstance.instance(), subNodePath, buildValue(TransactionState.COMMIT.name())) == null) {
                throw new TransactionException("abort transaction failed. path=" + subNodePath);
            }
        }
    }
}