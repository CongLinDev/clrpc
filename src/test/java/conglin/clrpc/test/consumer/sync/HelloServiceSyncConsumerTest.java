package conglin.clrpc.test.consumer.sync;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.test.service.HelloService;

/**
 * 测试字符串同步调用服务
 */
public class HelloServiceSyncConsumerTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();

        HelloService helloService = bootstrap.refreshAndSubscribe("HelloService", HelloService.class);
        String s = helloService.hello();
        System.out.println(s);

        bootstrap.stop();
        System.out.println("Consumer closing...");
    }
}