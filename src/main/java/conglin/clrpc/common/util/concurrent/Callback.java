package conglin.clrpc.common.util.concurrent;

public interface Callback{

    void success(Object result);

    void fail(Exception e);
}