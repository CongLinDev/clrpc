package conglin.clrpc.test.benchmark.async;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.service.proxy.ObjectProxy;

public class AsyncConsumerTimeTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();
        ObjectProxy objectProxy = bootstrap.subscribe("HelloService");

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