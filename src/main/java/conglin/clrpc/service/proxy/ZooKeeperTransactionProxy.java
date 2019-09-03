package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.service.future.RpcFuture;

public class ZooKeeperTransactionProxy implements TransactionProxy {

    @Override
    public TransactionProxy begin() {
        return null;
    }

    @Override
    public TransactionProxy call(String serviceName, String method, Object... args) {
        return null;
    }

    @Override
    public TransactionProxy call(String serviceName, Method method, Object... args) {
        return null;
    }

    @Override
    public RpcFuture commit() {
        return null;
    }

	@Override
	public boolean rollback() {
		throw new UnsupportedOperationException();
	}

}