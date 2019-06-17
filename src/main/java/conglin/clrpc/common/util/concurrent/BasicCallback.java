package conglin.clrpc.common.util.concurrent;

public interface BasicCallback{

    void success(Object result);

    void fail(Exception e);
}