package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.test.service.HelloService;

/**
 * 测试同步调用服务
 */
public class SyncConsumerTest1 {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();

        HelloService helloService = bootstrap.subscribe("HelloService", HelloService.class);
        String s = helloService.hello();
        System.out.println(s);

        bootstrap.stop();
        System.out.println("Consumer closing...");
    }
}