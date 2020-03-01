package conglin.clrpc.test.consumer.sync;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.service.UserService;

/**
 * 测试POJO同步调用服务
 */
public class UserServiceSyncConsumerTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();

        UserService userService = bootstrap.refreshAndSubscribe("UserService", UserService.class);
        User user = userService.getUser(1256L, "小明");
        System.out.println(user);
        System.out.println("-------------------------");

        bootstrap.stop();
        System.out.println("Consumer closing...");
    }
}