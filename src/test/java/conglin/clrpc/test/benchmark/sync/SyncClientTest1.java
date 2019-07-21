package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.test.service.HelloService;

/**
 * 测试同步调用服务
 */
public class SyncClientTest1{
    public static void main(String[] args){
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();

        HelloService helloService = clientBootstrap.getService(HelloService.class);
        String s = helloService.hello();
        System.out.println(s);
        try{
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}