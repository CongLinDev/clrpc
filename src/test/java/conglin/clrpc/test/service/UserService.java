package conglin.clrpc.test.service;

import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.pojo.UserInfo;

public interface UserService {
    User getUser(Long userId, String username);

    String postUser(User user);

    String postUserInfo(UserInfo userInfo);
}