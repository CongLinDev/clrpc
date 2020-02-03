package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.UserServiceImpl;

/**
 * 测试未注册服务情况下 同步获取服务
 */
public class SyncProviderTest2 {
    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        try {
            System.out.println("Provider opening...");
            // 未添加服务
            bootstrap.publish("UserService", new UserServiceImpl()).start();

        } finally {
            bootstrap.stop();
            System.out.println("Provider closing...");
        }
    }
}