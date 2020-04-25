package conglin.clrpc.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.common.Callback;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.test.service.EchoService;

public class ConsumerThreadTest {

    public static void main(String[] args) throws Exception {
        echoServiceTest(5, 100000);
    }

    protected static void echoServiceTest(final int thread, final int requestNumber) throws Exception {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();

        final List<Long> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(thread);
        EchoService echoService = bootstrap.refreshAndSubscribeAsync(EchoService.class);
        Runnable task = () -> {
            final long begin = System.currentTimeMillis();
            for(int i = 0; i < requestNumber; i++) {
                echoService.echoNull();
                // echoService.echoPOJO(new User(Long.MAX_VALUE, "conglin"));
                // echoService.echoBytes(new byte[1000]);
            }
            AsyncObjectProxy.lastFuture().callback(Callback.convert(()->{
                long end = System.currentTimeMillis();
                result.add(end-begin);
            }));
            latch.countDown();
        };

        for(int i = 0; i < thread; i++) {
            new Thread(task).start();
        }

        latch.await();
        
        bootstrap.stop();

        long count = 0L;
        for(Long value : result) {
            System.out.print(" " + value);
            count += value;
        }

        System.out.println("\ncount: " + thread * requestNumber * 1000.0 / count);
        
    }
}