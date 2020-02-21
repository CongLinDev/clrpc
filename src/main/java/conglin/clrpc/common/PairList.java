package conglin.clrpc.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PairList<T, P, Q> extends ArrayList<Pair<P, Q>> {

    private static final long serialVersionUID = 6141207565695023505L;

    private T listInfo;

    public PairList(T listInfo) {
        super();
        this.listInfo = listInfo;
    }

    public PairList(T listInfo, int initialCapacity) {
        super(initialCapacity);
        this.listInfo = listInfo;
    }

    public PairList(T listInfo, Collection<Pair<P, Q>> pairCollection) {
        super(pairCollection);
        this.listInfo = listInfo;
    }

    /**
     * 获取列表元信息
     * 
     * @return
     */
    public T listInfo() {
        return listInfo;
    }

    /**
     * 获取列表Pair的首项列表
     * 
     * @return
     */
    public List<P> firstList() {
        return stream().map(Pair::getFirst).collect(Collectors.toList());
    }

    /**
     * 获取列表Pair的次项列表
     * 
     * @return
     */
    public List<Q> secondList() {
        return stream().map(Pair::getSecond).collect(Collectors.toList());
    }
}