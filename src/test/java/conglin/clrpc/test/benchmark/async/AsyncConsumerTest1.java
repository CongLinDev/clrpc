package conglin.clrpc.test.benchmark.async;

import java.util.Random;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.pojo.User;

/**
 * 测试异步调用服务
 */
public class AsyncConsumerTest1 {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();
        
        ObjectProxy objectProxy = bootstrap.subscribeServiceAsync("UserService");

        Random random = new Random();
        for(int i = 0; i < 10; i++){
            new Thread(() -> {
                final long id = random.nextLong();
                System.out.println(id);
                objectProxy.call("getUser", id, "小明").addCallback(new Callback() {

                    @Override
                    public void success(Object result) {
                        System.out.println(((User)result).toString());
                    }

                    @Override
                    public void fail(String remoteAddress, RpcServiceException e) {
                        System.out.println(remoteAddress + ": " + e.getMessage());
                    }

                });
            }).start();
        }
        
        try{
            bootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Consumer closing...");
    }
}