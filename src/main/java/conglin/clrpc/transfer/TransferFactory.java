package conglin.clrpc.transfer;

import conglin.clrpc.transfer.receiver.*;
import conglin.clrpc.transfer.sender.*;

public class TransferFactory{

    /**
     * 请求接收器
     */
    public enum RequestReceiverType{
        BASIC(new BasicRequestReceiver()), TRANSACTION(null);

        private RequestReceiver r;

        RequestReceiverType(RequestReceiver r){
            this.r = r;
        }

        public RequestReceiver getInstance(){
            return r;
        }
    }

    /**
     * 回复接收器
     */
    public enum ResponseReceiverType{
        BASIC(new BasicResponseReceiver()), TRANSACTION(null);

        private ResponseReceiver r;

        ResponseReceiverType(ResponseReceiver r){
            this.r = r;
        }

        public ResponseReceiver getInstance(){
            return r;
        }
    }

    /**
     * 请求发送器
     */
    public enum RequestSenderType{

        BASIC(new BasicRequestSender()), TRANSACTION(null);

        private RequestSender s;

        RequestSenderType(RequestSender s){
            this.s = s;
        }

        public RequestSender getInstance(){
            return s;
        }
    }

    /**
     * 回复发送器
     */
    public enum ResponseSenderType{
        BASIC(new BasicResponseSender()), TRANSACTION(null);

        private ResponseSender s;

        ResponseSenderType(ResponseSender s){
            this.s = s;
        }

        public ResponseSender getInstance(){
            return s;
        }
    }

}