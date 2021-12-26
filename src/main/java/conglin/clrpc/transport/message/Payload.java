package conglin.clrpc.transport.message;

/**
 * 消息类
 *
 * 具体的消息类必须覆盖 {@link Payload#payloadType()} 方法
 *
 * 若将具体消息用于传输通信，则可以调用
 * {@link conglin.clrpc.global.GlobalPayloadManager#setPayloadClass(int, Class)}
 */
public interface Payload {

    /**
     * 继承该类的子类，必须设定一个消息类型码 默认为7个比特位
     *
     * 其中 [0,0x1F] 被保留
     */
    int PAYLOAD_TYPE_MASK = 0x7f;

    /**
     * payload type
     *
     * @return
     */
    int payloadType();
}
