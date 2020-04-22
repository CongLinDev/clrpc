package conglin.clrpc.transport.message;

public class CacheableResponse extends BasicResponse {

    private static final long serialVersionUID = 8675701922448321682L;

    transient public static final int MESSAGE_TYPE = 4;

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    private int flag;

    public CacheableResponse(Long messageId, boolean error, Object result) {
        super(messageId, error, result);
    }

    public CacheableResponse(BasicResponse response) {
        super(response);
        this.flag = 0;
    }

    public CacheableResponse(CacheableResponse response) {
        super(response);
        this.flag = response.getFlag();
    }

    /**
     * 用于复制缓存结果
     * 
     * @param messageId
     * @return
     */
    public CacheableResponse copy(Long messageId) {
        CacheableResponse response = new CacheableResponse(messageId, isError(), result());
        response.setFlag(this.flag);
        return response;
    }

    /**
     * 以下方法均为设置或获取 {@link conglin.clrpc.transport.message.CacheableResponse#flag} 的方法
     * 
     * {@link CacheableResponse#flag} 共 32 位 从低到高为 [0 - 31] 各比特位的含义如下：
     * 
     * <pre>
     * 比特位          含义
     * 
     *  [ 0 ]       消息幂等性位，若为1说明请求的方法为幂等性方法。
     *  [ 1 ]       接收者是否可以缓存本消息，即认为接收者可以认为本消息在一段时间内有效。
     *  [ 2 ]       发送者是否可以缓存本消息，即认为发送者可以认为本消息在一段时间内有效。
     *  [ 3 - 31 ]  共 29 位，若比特位[1] 或 比特位[2] 为 1 时有效。代表的消息有效时间。单位为毫秒。
     *              其中最小为 1 毫秒， 最大为 (2 ^ 29 - 1) 毫秒。（0 为最大缓存时间即理论上的无限期）
     * </pre>
     */

    /**
     * 直接获得 {@link CacheableResponse#flag} 的值
     * 
     * @return
     */
    protected int getFlag() {
        return flag;
    }

    /**
     * 直接设置 {@link CacheableResponse#flag} 的值
     * 
     * @return
     */
    protected void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * 查找 {@link CacheableResponse#flag} 的某一比特位是否填充 1
     * 
     * @param bit
     * @return
     */
    protected boolean isOneWithBit(int bit) {
        int curBit = (1 << bit);
        return (flag & curBit) == curBit;
    }

    /**
     * 为 {@link CacheableResponse#flag} 的某一比特位填充 1
     * 
     * @param bit
     * @return
     */
    protected void signOneWithBit(int bit) {
        flag |= (1 << bit);
    }

    /**
     * 判断Response消息是否是幂等性的 若是，则该消息对于消息的提供者和消费者来说 都可以进行无限期的缓存
     * 
     * @return
     */
    public boolean isIdempotent() {
        return isOneWithBit(0);
    }

    /**
     * 标记Response消息是幂等性的
     */
    public void signIdempotent() {
        signOneWithBit(0);
    }

    /**
     * Response消息能否被消息消费者即接收者缓存
     * 
     * @return
     */
    public boolean canCacheForConsumer() {
        return isOneWithBit(1);
    }

    /**
     * 标记Response消息能被消息消费者即接收者缓存
     */
    public void signCacheForConsumer() {
        signOneWithBit(1);
    }

    /**
     * Response消息能否被消息提供者即发送者缓存
     * 
     * @return
     */
    public boolean canCacheForProvider() {
        return isOneWithBit(2);
    }

    /**
     * 标记Response消息能被消息提供者即发送者缓存
     */
    public void signCacheForProvider() {
        signOneWithBit(2);
    }

    /**
     * 设置Response消息能被缓存的最大过期时间
     * 
     * @param exprie
     */
    public void setExpireTime(int exprie) {
        exprie = exprie << 3;
        flag &= 0x7;
        flag |= exprie;
    }

    /**
     * 获取Response消息能被消息消费者即接收者缓存的最大过期时间
     * 
     * @return
     */
    public int getExpireTime() {
        return flag >> 3;
    }
}