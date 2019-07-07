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

    /**
     * 分割host和端口
     * @param data
     * @return
     * @throws UnknownHostException
     */
    public static InetSocketAddress splitHostnameAndPort(String data) throws UnknownHostException {

        String [] hostnameAndPort = data.trim().split(":");
        if(hostnameAndPort.length != 2) throw new UnknownHostException(data);
        return InetSocketAddress.createUnresolved(hostnameAndPort[0], Integer.parseInt(hostnameAndPort[1]));
    }

    /**
     * 分割host和端口
     * @param data
     * @return
     */
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

    /**
     * 静默地分割host和端口
     * 即遇到异常时不抛出异常而是返回空指针
     * @param data
     * @return
     */
    public static InetSocketAddress splitHostnameAndPortSilently(String data){
        try{
            return splitHostnameAndPort(data);
        }catch(UnknownHostException e){
            log.error("Error address = " + data + " And it can not be converted");
            return null;
        }
    }
}