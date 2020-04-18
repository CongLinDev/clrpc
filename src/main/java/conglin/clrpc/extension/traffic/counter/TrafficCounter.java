package conglin.clrpc.extension.traffic.counter;

public interface TrafficCounter {

    /**
     * 已经读的总字节数
     * 
     * @return
     */
    long readBytes();

    /**
     * 已经读的次数
     * 
     * @return
     */
    int readCounts();

    /**
     * 读字节
     * 
     * @param increase
     */
    void submitRead(int increase);

    /**
     * 已经写的总字节数
     * 
     * @return
     */
    long writeBytes();

    /**
     * 已经写的次数
     * 
     * @return
     */
    int writeCounts();

    /**
     * 写字节
     * 
     * @param increase
     */
    void submitWrite(int increase);

    /**
     * 计算差距
     * 
     * @param that
     * @return 差距
     */
    TrafficCounter calculateGap(TrafficCounter that);

    /**
     * 清空数据
     */
    void clear();

    default String jsonString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"readCounts\":").append(readCounts())
                .append(",\"readBytes\":").append(readBytes())
                .append(",\"writeCounts\":").append(writeCounts())
                .append(",\"writeBytes\":").append(writeBytes())
                .append("}");
        return builder.toString();
    }
}