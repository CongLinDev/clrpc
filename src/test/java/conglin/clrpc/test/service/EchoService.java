package conglin.clrpc.test.service;

import conglin.clrpc.service.annotation.Service;
import conglin.clrpc.test.pojo.User;

@Service(name = "EchoService")
public interface EchoService {

    void echoNull();

    byte[] echoBytes(byte[] data);

    User echoPOJO(User user);
}