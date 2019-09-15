package conglin.clrpc.transfer.message;

import java.io.Serializable;

public class BasicResponse extends Message implements Serializable{
    
    private static final long serialVersionUID = 7123186624198529783L;

    transient public static final int MESSAGE_TYPE = 1;
    
    protected Object result;
    protected int flag;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        if(isError()){
            return "BasicResponse [requestId=" + requestId + ", error=" + result + "]";
        }else{
            return "BasicResponse [requestId=" + requestId + ", result=" + result + "]";
        }
    }




    /**
     * 以下方法均为设置或获取 {@link BasicResponse#flag} 的方法
     * 
     * {@link BasicResponse#flag} 共 32 位 从低到高为 [0 - 31]
     * 各比特位的含义如下：
     *    比特位                                      含义
     * [    0    ]      Error位，若为1说明请求执行失败，{@link BasicResponse#result} 为抛出的异常。
     * [    1    ]      消息幂等性位，若为1说明请求的方法为幂等性方法。
     * [    2    ]      {@link BasicResponse}的接收者是否可以缓存本消息，即认为接收者可以认为本消息在一段时间内有效。
     * [    3    ]      {@link BasicResponse}的发送者是否可以缓存本消息，即认为发送者可以认为本消息在一段时间内有效。
     * [ 4 - 23  ]      共 20 位，若 比特位[2] 或 比特位[3] 为 1 时有效。代表的消息有效时间。单位为毫秒。
     *                  其中最小为 1 毫秒， 最大为 (2 ^ 20) 毫秒。（0 为 最大缓存时间即理论上的无限期）
     * 
     * [ 24 - 31 ]      保留位。
     */

    /**
     * 直接获得 {@link BasicResponse#flag} 的值
     * @return
     */
    public int getFlag(){
        return flag;
    }

    /**
     * 直接设置 {@link BasicResponse#flag} 的值
     * @return
     */
    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * 查找 {@link BasicResponse#flag} 的某一比特位是否填充 1
     * @param bit
     * @return
     */
    protected boolean isOneWithBit(int bit){
        int curBit = (1 << bit);
        return (flag & curBit) == curBit;
    }

    /**
     * 为 {@link BasicResponse#flag} 的某一比特位填充 1
     * @param bit
     * @return
     */
    protected void signOneWithBit(int bit){
        flag |= (1 << bit);
    }

    /**
     * Response消息是否出现错误
     * @return
     */
    public boolean isError() {
        return isOneWithBit(0);
    }

    /**
     * 标记Response消息出现了错误
     * 调用 {@link BasicResponse#signError()} 后
     * 应当再调用 {@link BasicResponse#setResult(Object)} 保存异常
     */
    public void signError() {
        signOneWithBit(0);
    }

    /**
     * 判断Response消息是否是幂等性的
     * 若是，则该消息对于消息的提供者和消费者来说
     * 都可以进行无限期的缓存
     * @return
     */
    public boolean isIdempotent(){
        return isOneWithBit(1);
    }

    /**
     * 标记Response消息是幂等性的
     */
    public void signIdempotent(){
        signOneWithBit(1);
    }

    /**
     * Response消息能否被消息消费者即接收者缓存
     * @return
     */
    public boolean canCacheForConsumer(){
        return isOneWithBit(2);
    }

    /**
     * 标记Response消息能被消息消费者即接收者缓存
     */
    public void signCacheForConsumer(){
        signOneWithBit(2);
    }

    /**
     * Response消息能否被消息提供者即发送者缓存
     * @return
     */
    public boolean canCacheForProvider(){
        return isOneWithBit(3);
    }

    /**
     * 标记Response消息能被消息提供者即发送者缓存
     */
    public void signCacheForProvider(){
        signOneWithBit(3);
    }

    /**
     * 设置Response消息能被缓存的最大过期时间
     * @param exprie
     */
    public void setExpireTime(int exprie){
        exprie = (exprie << 3) & 0x7FFFF8;
        flag &= ~0x7FFFF8;
        flag |= exprie;
    }

    /**
     * 获取Response消息能被消息消费者即接收者缓存的最大过期时间
     * @return
     */
    public int getExpireTime(){
        return (flag & 0x7FFFF8) >> 3;
    }
}