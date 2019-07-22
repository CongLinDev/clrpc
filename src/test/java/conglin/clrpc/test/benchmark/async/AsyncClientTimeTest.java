package conglin.clrpc.test.benchmark.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.service.HelloService;

public class AsyncClientTimeTest {
    public static void main(String[] args) {
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();
        ObjectProxy objectProxy = clientBootstrap.getAsynchronousService(HelloService.class);

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        long start = System.currentTimeMillis();
        for(int i = 0; i < 1000; i++){
            executorService.submit(()->{
                objectProxy.call("hello");
            });
        }
        executorService.shutdown();

        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - start) + " ms");
        
        try{
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}