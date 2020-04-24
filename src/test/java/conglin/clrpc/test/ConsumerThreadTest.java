package conglin.clrpc.test;

import java.util.concurrent.CountDownLatch;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.common.Callback;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.test.service.EchoService;

public class ConsumerThreadTest {

    public static void main(String[] args) throws Exception {
        echoServiceTest(2, 100000);
    }

    protected static void echoServiceTest(final int thread, final int requestNumber) throws Exception {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();

        CountDownLatch latch = new CountDownLatch(thread);
        EchoService echoService = bootstrap.refreshAndSubscribeAsync(EchoService.class);
        Runnable task = () -> {
            final long begin = System.currentTimeMillis();
            for(int i = 0; i < requestNumber; i++) {
                echoService.echoNull();
            }
            AsyncObjectProxy.lastFuture().callback(Callback.convert(()->{
                long end = System.currentTimeMillis();
                System.out.println(end - begin);
            }));
            latch.countDown();
        };

        for(int i = 0; i < thread; i++) {
            new Thread(task).start();
        }

        latch.await();
        
        bootstrap.stop();
    }
}