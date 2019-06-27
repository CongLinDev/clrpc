package conglin.clrpc.test.pojo;

public class UserInfo{
    private User user;

    private String password;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserInfo [password=" + password + ", user=" + user + "]";
    }

    public UserInfo(User user, String password) {
        this.user = user;
        this.password = password;
    }
}