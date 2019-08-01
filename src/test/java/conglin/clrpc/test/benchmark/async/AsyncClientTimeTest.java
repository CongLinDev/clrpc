package conglin.clrpc.test.benchmark.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.common.util.concurrent.Callback;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.service.HelloService;

public class AsyncClientTimeTest {
    public static void main(String[] args) {
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();
        ObjectProxy objectProxy = clientBootstrap.getAsynchronousService(HelloService.class);

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        final CountDownLatch countDownLatch = new CountDownLatch(1000);
        long start = System.currentTimeMillis();
        for(int i = 0; i < 1000; i++){
            executorService.submit(()->{
                objectProxy.call("hello").addCallback(new Callback() {

                    @Override
                    public void success(Object result) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void fail(Exception e) {
                        System.out.println(e);
                        countDownLatch.countDown();
                    }

                });
            });
        }
                
        try{
            countDownLatch.await();
            long end = System.currentTimeMillis();
            System.out.println("Waste time: " + (end - start) + " ms");
            executorService.shutdown();
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}