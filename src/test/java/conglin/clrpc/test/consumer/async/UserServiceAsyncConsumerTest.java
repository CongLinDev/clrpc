package conglin.clrpc.test.consumer.async;

import java.util.Random;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.common.Callback;
import conglin.clrpc.service.proxy.ObjectProxy;

/**
 * 测试异步调用服务
 */
public class UserServiceAsyncConsumerTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();

        ObjectProxy objectProxy = bootstrap.refreshAndSubscribe("UserService");

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                final long id = random.nextLong();
                System.out.println(id);
                objectProxy.call("getUser", id, "小明").addCallback(new Callback() {

                    @Override
                    public void success(Object result) {
                        System.out.println(result.toString());
                    }

                    @Override
                    public void fail(Exception e) {
                        System.out.println(e.getMessage());
                    }

                });
            }).start();
        }

        bootstrap.stop();
        System.out.println("Consumer closing...");
    }
}