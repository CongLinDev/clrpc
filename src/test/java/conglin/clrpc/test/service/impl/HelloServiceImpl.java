package conglin.clrpc.test.service.impl;

import conglin.clrpc.test.service.HelloService;

public class HelloServiceImpl implements HelloService{
    @Override
    public String hello() {
        System.out.println(System.currentTimeMillis() + "      hello");
        return "hello-consumer";
    }
}