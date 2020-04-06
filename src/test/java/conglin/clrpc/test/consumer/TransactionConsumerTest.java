package conglin.clrpc.test.consumer;

import java.util.concurrent.ExecutionException;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.common.Callback;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import conglin.clrpc.test.service.HelloService;
import conglin.clrpc.test.service.UserService;

public class TransactionConsumerTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();
        TransactionProxy proxy = bootstrap.subscribeTransaction();
        bootstrap.refresh(HelloService.class).refresh(UserService.class);
        
        proxy.begin();
        HelloService helloSerivce = proxy.subscribeAsync(HelloService.class);
        UserService userService = proxy.subscribeAsync(UserService.class);

        helloSerivce.hello();
        RpcFuture f1 = AsyncObjectProxy.lastFuture();

        userService.getUser(1256L, "xiaoming");
        RpcFuture f2 = AsyncObjectProxy.lastFuture();

        try {
            System.out.println("sleep...");
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RpcFuture future = proxy.commit();

        future.addCallback(new Callback() {
            @Override
            public void success(Object result) {
                System.out.println("success...");
            }

            @Override
            public void fail(Exception exception) {
                System.out.println("fail...");
            }
        });
        try {
            System.out.println(future.get());
            System.out.println(f1.get());
            System.out.println(f2.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        bootstrap.stop();
    }
}