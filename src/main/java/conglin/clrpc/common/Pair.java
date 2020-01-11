package conglin.clrpc.common;

public class Pair<P, Q> {
    private P p;
    private Q q;

    public Pair() {

    }

    public Pair(P p, Q q) {
        this.p = p;
        this.q = q;
    }

    /**
     * 获得第一个值
     * 
     * @return
     */
    public P getFirst() {
        return p;
    }

    /**
     * 设置第一个值
     * 
     * @param first
     */
    public void setFirst(P first) {
        this.p = first;
    }

    /**
     * 获得第二个值
     * 
     * @return
     */
    public Q getSecond() {
        return q;
    }

    /**
     * 设置第二个值
     * 
     * @param second
     */
    public void setSecond(Q second) {
        this.q = second;
    }
}