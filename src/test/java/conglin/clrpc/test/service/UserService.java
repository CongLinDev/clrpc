package conglin.clrpc.test.service;

import conglin.clrpc.test.pojo.User;

public interface UserService {
    User getUser(Long userId, String username);

    String postUser(User user);
}