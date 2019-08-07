package conglin.clrpc.test.benchmark.async;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.common.util.concurrent.Callback;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.pojo.User;

/**
 * 测试异步调用服务
 */
public class AsyncClientTest1 {
    public static void main(String[] args) {
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();
        
        ObjectProxy objectProxy = clientBootstrap.getAsynchronousService("UserService");

        Random random = new Random();
        final CountDownLatch countDownLatch = new CountDownLatch(10);
        for(int i = 0; i < 10; i++){
            new Thread(() -> {
                final long id = random.nextLong();
                System.out.println(id);
                objectProxy.call("getUser", id, "小明").addCallback(new Callback() {

                    @Override
                    public void success(Object result) {
                        System.out.println(((User)result).toString());
                        countDownLatch.countDown();
                    }

                    @Override
                    public void fail(String remoteAddress, Exception e) {
                        System.out.println(remoteAddress + ": " + e.getMessage());
                        countDownLatch.countDown();
                    }

                });
            }).start();
        }
        
        try{
            countDownLatch.await();
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}