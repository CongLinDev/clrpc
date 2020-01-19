package conglin.clrpc.test.benchmark.transaction;

import java.util.concurrent.ExecutionException;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RequestException;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.proxy.TransactionProxy;

public class ConsumerTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();
        TransactionProxy proxy = bootstrap.subscribeTransaction();

        proxy.begin().call("HelloService", "hello").call("UserService", "getUser", 1256L, "小明");

        try {
            System.out.println("sleep...");
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis());
        RpcFuture future = proxy.commit();

        future.addCallback(new Callback() {
            @Override
            public void success(Object result) {
                System.out.println("success...");
            }

            @Override
            public void fail(Exception exception) {
                System.out.println("fail");
            }
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException | RequestException e) {
            e.printStackTrace();
        }
        bootstrap.stop();
        System.out.println("Consumer closing...");
    }
}