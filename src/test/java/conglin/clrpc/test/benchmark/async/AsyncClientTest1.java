package conglin.clrpc.test.benchmark.async;

import java.util.Random;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.common.util.concurrent.Callback;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.service.UserService;

/**
 * 测试异步调用服务
 */
public class AsyncClientTest1 {
    public static void main(String[] args) {
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();
        
        ObjectProxy objectProxy = clientBootstrap.getAsynchronousService(UserService.class);

        Random random = new Random();

        for(int i = 0; i < 10; i++){
            new Thread(() -> {
                final long id = random.nextLong();
                System.out.println(id);
                objectProxy.call("getUser", id, "小明").addCallback(new Callback() {

                    @Override
                    public void success(Object result) {
                        System.out.println(result);
                    }

                    @Override
                    public void fail(Exception e) {
                        System.out.println(e);
                    }

                });
            }).start();
        }

        try{
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}