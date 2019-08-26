package conglin.clrpc.test.benchmark.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.service.HelloService;

public class AsyncConsumerTimeTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();
        ObjectProxy objectProxy = bootstrap.subscribeServiceAsync(HelloService.class);

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
                    public void fail(String remoteAddress, RpcServiceException e) {
                        System.out.println(remoteAddress + ": " + e.getMessage());
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
            bootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Consumer closing...");
    }
}