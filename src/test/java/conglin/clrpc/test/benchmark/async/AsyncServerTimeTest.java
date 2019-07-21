package conglin.clrpc.test.benchmark.async;

import conglin.clrpc.bootstrap.RpcServerBootstrap;
import conglin.clrpc.test.service.HelloService;
import conglin.clrpc.test.service.impl.HelloServiceImpl;

public class AsyncServerTimeTest {
    public static void main(String[] args) {
        RpcServerBootstrap serverBootstrap = new RpcServerBootstrap();
        try {
            System.out.println("Server opening...");
            serverBootstrap.addService(HelloService.class, HelloServiceImpl.class)
                    .start();
            
        }finally{
            serverBootstrap.stop();
            System.out.println("Server closing...");
        }
    }
}