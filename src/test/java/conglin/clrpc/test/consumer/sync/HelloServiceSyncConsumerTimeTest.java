package conglin.clrpc.test.consumer.sync;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.test.service.HelloService;

public class HelloServiceSyncConsumerTimeTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();

        HelloService helloService = bootstrap.refreshAndSubscribe(HelloService.class);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            helloService.hello();
        }
        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - start) + " ms");

        bootstrap.stop();
    }
}