package conglin.clrpc.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.common.Callback;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.service.EchoService;
import conglin.clrpc.test.service.FileService;
import conglin.clrpc.test.service.HelloService;
import conglin.clrpc.test.service.UserService;

public class ConsumerTest {

    public static void main(String[] args) {
        echoServiceAsyncTimeTest();
    }

    protected static void echoServiceAsyncTimeTest() {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();
        EchoService echoService = bootstrap.refreshAndSubscribeAsync(EchoService.class);
        long begin = System.currentTimeMillis();
        System.out.println("begin:" + begin);
        for (int i = 0; i < 10000; i++) {
            echoService.echoNull();
        }
        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - begin) + " ms");
        bootstrap.stop();
    }

    protected static void helloServiceSyncTest() {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();

        HelloService helloService = bootstrap.refreshAndSubscribe(HelloService.class);
        String s = helloService.hello();
        System.out.println(s);

        bootstrap.stop();
    }

    protected static void helloServiceSyncTimeTest() {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();
        HelloService helloService = bootstrap.refreshAndSubscribe(HelloService.class);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            helloService.hello();
        }
        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - begin) + " ms");

        bootstrap.stop();
    }

    protected static void helloServiceAsyncTimeTest() {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();
        HelloService service = bootstrap.refreshAndSubscribeAsync(HelloService.class);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            service.hello();
        }

        bootstrap.stop();
        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - begin) + " ms");
    }

    protected static void userServiceSyncTest() {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();
        UserService userService = bootstrap.refreshAndSubscribe(UserService.class);
        User user = userService.getUser(1256L, "小明");
        System.out.println(user);
        bootstrap.stop();
    }

    protected static void userServiceAsyncTest() {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();
        UserService service = bootstrap.refreshAndSubscribeAsync(UserService.class);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                service.getUser(1024L, "小明");
                AsyncObjectProxy.lastFuture().addCallback(new Callback() {

                    @Override
                    public void success(Object result) {
                        System.out.println(result.toString());
                    }

                    @Override
                    public void fail(Exception e) {
                        System.out.println(e.getMessage());
                    }

                });
            }).start();
        }
        bootstrap.stop();
    }

    protected static void fileServiceSyncTest() {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();

        FileService fileService = bootstrap.refreshAndSubscribe(FileService.class);

        try (InputStream inputStream = new FileInputStream(new File("architecture/architecture.png"))) {
            byte[] bytes = inputStream.readAllBytes();
            String s = fileService.receiveFile("arch.jpg", bytes);
            System.out.println(s);
            bootstrap.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void transactionTest() {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        bootstrap.start();
        TransactionProxy proxy = bootstrap.subscribeTransaction();
        bootstrap.refresh(HelloService.class).refresh(UserService.class);

        proxy.begin();
        HelloService helloSerivce = proxy.subscribeAsync(HelloService.class);
        UserService userService = proxy.subscribeAsync(UserService.class);

        helloSerivce.hello();
        RpcFuture f1 = AsyncObjectProxy.lastFuture();

        userService.getUser(1256L, "xiaoming");
        RpcFuture f2 = AsyncObjectProxy.lastFuture();

        try {
            System.out.println("sleep...");
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RpcFuture future = proxy.commit();

        future.addCallback(new Callback() {
            @Override
            public void success(Object result) {
                System.out.println("success...");
            }

            @Override
            public void fail(Exception exception) {
                System.out.println("fail...");
            }
        });
        try {
            System.out.println(future.get());
            System.out.println(f1.get());
            System.out.println(f2.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        bootstrap.stop();
    }

}