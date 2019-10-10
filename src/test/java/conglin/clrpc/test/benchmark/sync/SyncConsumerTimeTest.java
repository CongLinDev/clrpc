package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.test.service.HelloService;

public class SyncConsumerTimeTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();

        HelloService helloService = bootstrap.subscribe(HelloService.class);

        long start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++){
            helloService.hello();
        }
        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - start) + " ms");

        try{
            bootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Consumer closing...");
    }
}