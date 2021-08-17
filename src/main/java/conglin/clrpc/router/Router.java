package conglin.clrpc.router;

public interface Router<T> {
    T choose();
}
