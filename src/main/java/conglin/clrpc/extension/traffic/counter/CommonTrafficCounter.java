package conglin.clrpc.extension.traffic.counter;

public class CommonTrafficCounter implements TrafficCounter {

    private long readBytes; // 读的总字节数
    private int readCounts; // 读的次数

    private long writeBytes; // 写的总字节数
    private int writeCounts; // 写的总次数

    public CommonTrafficCounter() {
        this(0, 0, 0, 0);
    }

    /**
     * @param counter
     */
    public CommonTrafficCounter(TrafficCounter counter) {
        this(counter.readBytes(), counter.readCounts(), counter.writeBytes(), counter.writeCounts());
    }

    /**
     * 构造一个 {@link CommonTrafficCounter}
     * 
     * @param readBytes   已经读的字节数
     * @param readCounts  已经读的次数
     * @param writeBytes  已经写的字节数
     * @param writeCounts 已经写的次数
     */

    public CommonTrafficCounter(long readBytes, int readCounts, long writeBytes, int writeCounts) {
        this.readBytes = readBytes;
        this.readCounts = readCounts;
        this.writeBytes = writeBytes;
        this.writeCounts = writeCounts;
    }

    @Override
    public long readBytes() {
        return readBytes;
    }

    @Override
    public int readCounts() {
        return readCounts;
    }

    @Override
    public void submitRead(int increase) {
        if (increase < 0)
            return;
        readBytes += increase;
        readCounts++;
    }

    @Override
    public long writeBytes() {
        return writeBytes;
    }

    @Override
    public int writeCounts() {
        return writeCounts;
    }

    @Override
    public void submitWrite(int increase) {
        if (increase < 0)
            return;
        writeBytes += increase;
        writeCounts++;
    }

    @Override
    public CommonTrafficCounter calculateGap(TrafficCounter that) {
        if (that == null)
            return this;
        return new CommonTrafficCounter(readBytes() - that.readBytes(), readCounts() - that.readCounts(),
                writeBytes() - that.writeBytes(), writeCounts() - that.writeCounts());
    }

    @Override
    public void clear() {
        readBytes = 0L;
        readCounts = 0;
        writeBytes = 0L;
        writeCounts = 0;
    }

    @Override
    public String toString() {
        return jsonString();
    }
}
