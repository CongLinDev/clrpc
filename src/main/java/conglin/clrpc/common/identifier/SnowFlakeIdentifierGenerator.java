package conglin.clrpc.common.identifier;

/**
 * Twitter 的 SnowFalke算法实现
 * 
 * 对于64位的标识符：最高位不用，置为0；次41位作为当前的毫秒数；再次10位作为工作机器的ID；低12位作为序列号。
 * 
 * <pre>
 * |  作用    |  保留  |  当前的毫秒数   | 机房ID |  机器ID   |  序列号  |
 * | 占用位数 |    1   |       41      |   5    |    5    |    12   |
 * </pre>
 */
public class SnowFlakeIdentifierGenerator implements IdentifierGenerator {

    // 代表着机房ID和机器ID的应用ID，其机房ID和机器ID有效位的位置与生成的ID位置相同
    private long applicationId;

    // 上次使用的时间戳
    private long lastTimeStamp;

    // 上次使用的序列号
    private int serialId;

    public SnowFlakeIdentifierGenerator(int applicationId) {
        // 取低10位，放入 this.applicationId 中
        this.applicationId |= ((applicationId & 0x3ff) << 12);
    }

    /**
     * 更新时间戳和序列号
     */
    private void updateTimeStamp() {
        long currentTimeStamp = System.currentTimeMillis();
        if (currentTimeStamp != lastTimeStamp) {
            lastTimeStamp = currentTimeStamp;
            serialId = 0;
        } else {
            serialId = (++serialId) & 0xfff;
        }
    }

    @Override
    public long generate() {
        return generate(null);
    }

    @Override
    public long generate(String key) {
        updateTimeStamp();
        return (lastTimeStamp << 22) | applicationId | serialId;
    }

}