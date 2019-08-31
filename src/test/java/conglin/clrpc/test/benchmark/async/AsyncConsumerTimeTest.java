package conglin.clrpc.test.benchmark.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.service.HelloService;

public class AsyncConsumerTimeTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();
        ObjectProxy objectProxy = bootstrap.subscribeServiceAsync("HelloService");

        long start = System.currentTimeMillis();
        for(int i = 0; i < 1000; i++){
            objectProxy.call("hello");
        }
                
        try{
            bootstrap.stop();
            long end = System.currentTimeMillis();
            System.out.println("Waste time: " + (end - start) + " ms");
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Consumer closing...");
    }
}