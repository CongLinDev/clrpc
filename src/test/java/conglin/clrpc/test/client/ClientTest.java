package conglin.clrpc.test.client;

import java.util.Random;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.common.util.concurrent.Callback;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.service.HelloService;
import conglin.clrpc.test.service.UserService;


public class ClientTest{
    public static void main(String[] args) {
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();

        HelloService helloService = clientBootstrap.getService(HelloService.class);
        String s = helloService.hello();
        System.out.println(s);
        System.out.println("===================================");
        
        UserService userService = clientBootstrap.getService(UserService.class);
        User user = userService.getUser(1256L, "小明");
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