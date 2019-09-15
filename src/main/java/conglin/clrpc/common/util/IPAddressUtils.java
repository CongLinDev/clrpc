package conglin.clrpc.common.util;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPAddressUtils{

    private static final Logger log = LoggerFactory.getLogger(IPAddressUtils.class);

    public static InetSocketAddress splitHostnameAndPortResolved(String data) throws UnknownHostException{
        String[] hostnameAndPort = data.trim().split(":");
        if(hostnameAndPort.length != 2) throw new UnknownHostException(data);
        return new InetSocketAddress(hostnameAndPort[0], Integer.parseInt(hostnameAndPort[1]));
    }

    /**
     * 分割host和端口
     * @param data
     * @return
     * @throws UnknownHostException
     */
    public static InetSocketAddress splitHostnameAndPort(String data) throws UnknownHostException {
        String[] hostnameAndPort = data.trim().split(":");
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

    /**
     * 获取端口号
     * @param data
     * @return
     */
    public static int getPort(String data){
        String [] hostnameAndPort = data.trim().split(":");
        return Integer.parseInt(hostnameAndPort[1]);
    }

    /**
     * 获取主机名
     * @param data
     * @return
     */
    public static String getHostname(String data){
        String [] hostnameAndPort = data.trim().split(":");
        return hostnameAndPort[0];
    }
}