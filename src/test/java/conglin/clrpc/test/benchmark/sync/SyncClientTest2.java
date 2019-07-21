package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcClientBootstrap;
import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.service.UserService;

/**
 * 测试未注册服务情况下
 * 同步获取服务
 */
public class SyncClientTest2 {
    public static void main(String[] args) {
        RpcClientBootstrap clientBootstrap = new RpcClientBootstrap();
        System.out.println("Client opening...");
        clientBootstrap.start();
        
        UserService userService = clientBootstrap.getService(UserService.class);
        User user = userService.getUser(1256L, "小明");
        System.out.println(user);
        System.out.println("-------------------------");

        try{
            clientBootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Client closing...");
    }
}