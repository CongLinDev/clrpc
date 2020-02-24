package conglin.clrpc.test.provider;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.FileServiceImpl;

public class FileServiceProviderTest {
    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        try {
            System.out.println("Provider opening...");
            bootstrap.publish("FileService", new FileServiceImpl()).start();

        } finally {
            bootstrap.stop();
            System.out.println("Provider closing...");
        }
    }
}