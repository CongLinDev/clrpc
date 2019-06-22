package conglin.clrpc.test.client;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
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
        System.out.println(user);
        System.out.println("++++++++++++++++++++++++++++++++++++");
        System.out.println(userService.postUser(new User(1566L, "小刚")));

        try{
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}