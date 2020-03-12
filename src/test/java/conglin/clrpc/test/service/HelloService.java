package conglin.clrpc.test.service;

import conglin.clrpc.service.annotation.Service;

@Service(name = "HelloService")
public interface HelloService {
    /**
     * 返回一个字符串
     * 
     * @return
     */
    String hello();
}