package conglin.clrpc.transport.message;

import java.io.Serializable;

/**
 * 消息 payload 接口
 */
public interface Payload extends Serializable {

    /**
     * 继承该类的子类，必须设定一个消息类型码 默认为7个比特位
     *
     * 其中 [0,0x1F] 被保留
     */
    int PAYLOAD_TYPE_MASK = 0x7f;
}
