package conglin.clrpc.transfer.net;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class BasicResponse{
    private String requestId;
    private String error;
    private Object result;

    public boolean isError(){
        return (error != null);
    }
}