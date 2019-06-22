package conglin.clrpc.test.service.impl;

import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.service.UserService;

public class UserServiceImpl implements UserService {

    @Override
    public User getUser(Long userId, String username) {
        return new User(userId, username);
    }

    @Override
    public String postUser(User user) {
        System.out.println(user+"--------------------");
        return "ok";
    }

}