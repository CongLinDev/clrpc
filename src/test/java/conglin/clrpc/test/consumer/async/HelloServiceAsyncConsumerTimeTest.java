package conglin.clrpc.test.consumer.async;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.service.HelloService;

public class HelloServiceAsyncConsumerTimeTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();
        ObjectProxy objectProxy = bootstrap.refreshAndSubscribeAsync(HelloService.class);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            objectProxy.call("hello");
        }

        bootstrap.stop();
        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - start) + " ms");

        System.out.println("Consumer closing...");
    }
}