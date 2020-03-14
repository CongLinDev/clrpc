package conglin.clrpc.test.consumer.async;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.test.service.HelloService;

public class HelloServiceAsyncConsumerTimeTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();
        HelloService service = bootstrap.refreshAndSubscribeAsync(HelloService.class);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            service.hello();
        }

        bootstrap.stop();
        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - start) + " ms");
    }
}