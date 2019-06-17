package conglin.clrpc.test.client;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.test.service.HelloService;


public class ClientTest{
    public static void main(String[] args) {
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();

        HelloService service = clientBootstrap.getService(HelloService.class);
        String s = service.hello();
        System.out.println(s);
        try{
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}