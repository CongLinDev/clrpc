package conglin.clrpc.test.service.impl;

import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.service.EchoService;

public class EchoServiceImpl implements EchoService {

    @Override
    public void echoNull() {
    }

    @Override
    public byte[] echoBytes(byte[] data) {
        return data;
    }

    @Override
    public User echoPOJO(User user) {
        return user;
    }
}