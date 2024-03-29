package conglin.clrpc.common.object;

/**
 * 对象对
 * 
 * @param <P>
 * @param <Q>
 */
public class Pair<P, Q> {
    private final P p;
    private final Q q;

    public Pair(P p, Q q) {
        this.p = p;
        this.q = q;
    }

    public Pair(Pair<? extends P, ? extends Q> pair) {
        this.p = pair.getFirst();
        this.q = pair.getSecond();
    }

    /**
     * 获得第一个值
     * 
     * @return
     */
    public final P getFirst() {
        return p;
    }

    /**
     * 获得第二个值
     * 
     * @return
     */
    public final Q getSecond() {
        return q;
    }

    @Override
    public int hashCode() {
        if (p == null || q == null)
            return 0;
        return p.hashCode() ^ q.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Pair))
            return false;
        @SuppressWarnings("unchecked")
        Pair<P, Q> pair = (Pair<P, Q>) obj;
        return pair.getFirst().equals(p) && pair.getSecond().equals(q);
    }
}