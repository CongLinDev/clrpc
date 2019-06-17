package conglin.clrpc.common.util.net;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPAddressUtil{

    private static final Logger log = LoggerFactory.getLogger(IPAddressUtil.class);

    public static InetSocketAddress splitHostnameAndPort(String data) throws UnknownHostException {

        String [] hostnameAndPort = data.trim().split(":");
        if(hostnameAndPort.length != 2) throw new UnknownHostException(data);
        return InetSocketAddress.createUnresolved(hostnameAndPort[0], Integer.parseInt(hostnameAndPort[1]));
    }

    public static Set<InetSocketAddress> splitHostnameAndPort(List<String> data) {
        Set<InetSocketAddress> set = new HashSet<>(data.size());
        for(String s : data){
            try{
                InetSocketAddress address = splitHostnameAndPort(s);
                set.add(address);
            }catch(UnknownHostException e){
                log.error(e.getMessage());
            }          
        }
        return set;
    }
}