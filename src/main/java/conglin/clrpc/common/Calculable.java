package conglin.clrpc.common;

@FunctionalInterface
public interface Calculable<R> {
    /**
     * 计算
     * 
     * @return
     */
    R calculate();
}