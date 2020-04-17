package conglin.clrpc.common;

@FunctionalInterface
public interface Calculatable<R> {
    /**
     * 计算
     * 
     * @return
     */
    R calculate();
}