package conglin.clrpc.test.service;

import conglin.clrpc.test.pojo.User;
import conglin.clrpc.test.pojo.UserInfo;

public interface UserService {
    /**
     * 返回用户对象
     * 
     * @param userId
     * @param username
     * @return
     */
    User getUser(Long userId, String username);

    /**
     * 发送用户对象
     * 
     * @param user
     * @return
     */
    String postUser(User user);

    /**
     * 发送用户信息对象
     * 
     * @param userInfo
     * @return
     */
    String postUserInfo(UserInfo userInfo);
}