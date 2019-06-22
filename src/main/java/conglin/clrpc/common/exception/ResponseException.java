package conglin.clrpc.common.exception;

import conglin.clrpc.transfer.net.message.BasicResponse;

public class ResponseException extends RuntimeException {

    private static final long serialVersionUID = -6271577347823133915L;
    private BasicResponse response;
    
    public ResponseException(BasicResponse response){
        this.response = response;
    }

    public BasicResponse getBasicResponse(){
        return response;
    }
}