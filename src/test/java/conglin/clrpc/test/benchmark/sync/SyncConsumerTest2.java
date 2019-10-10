package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.service.UserService;

/**
 * 测试未注册服务情况下
 * 同步获取服务
 */
public class SyncConsumerTest2 {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();
        
        UserService userService = bootstrap.subscribe(UserService.class);
        User user = userService.getUser(1256L, "小明");
        System.out.println(user);
        System.out.println("-------------------------");

        try{
            bootstrap.stop();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Consumer closing...");
    }
}