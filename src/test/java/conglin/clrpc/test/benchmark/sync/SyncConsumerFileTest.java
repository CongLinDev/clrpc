package conglin.clrpc.test.benchmark.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import conglin.clrpc.bootstrap.RpcConsumerBootstrap;
import conglin.clrpc.test.service.FileService;

public class SyncConsumerFileTest {
    public static void main(String[] args) {
        RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
        System.out.println("Consumer opening...");
        bootstrap.start();

        FileService fileService = bootstrap.subscribe(FileService.class);
        
        try(InputStream inputStream = new FileInputStream(new File("architecture/architecture.png"))){
            byte[] bytes = inputStream.readAllBytes();
            String s = fileService.receiveFile("arch.jpg", bytes);
            System.out.println(s);
            bootstrap.stop();
        }catch(IOException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("Consumer closing...");
    }
}