package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.test.service.HelloService;

public class SyncClientTimeTest {
    public static void main(String[] args) {
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();

        HelloService helloService = clientBootstrap.getService(HelloService.class);

        long start = System.currentTimeMillis();
        for(int i = 0; i < 1000; i++){
            helloService.hello();
        }
        long end = System.currentTimeMillis();
        System.out.println("Waste time: " + (end - start) + " ms");

        try{
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}