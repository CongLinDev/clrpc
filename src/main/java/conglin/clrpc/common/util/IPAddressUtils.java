package conglin.clrpc.common.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IPAddressUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPAddressUtils.class);

    private static final InetAddress LOCAL_HOST = getRealLocalHost();

    private IPAddressUtils() {
        // Unused.
    }

    /**
     * 获取真实的本地ip地址
     * 
     * @return
     */
    private static InetAddress getRealLocalHost() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();

                if (networkInterface.isUp()) {
                    Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();

                    while (addressEnumeration.hasMoreElements()) {
                        InetAddress address = addressEnumeration.nextElement();
                        if (!address.isLinkLocalAddress() && !address.isLoopbackAddress()
                                && address.isSiteLocalAddress()) {
                            return address;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.error("Get localhost failed. {}", e);
        }
        return InetAddress.getLoopbackAddress();
    }

    /**
     * 返回本地地址
     * 
     * @return
     */
    public static InetAddress localhost() {
        return LOCAL_HOST;
    }

    /**
     * 返回地址和给定端口号拼接的字符串
     * 
     * @param address
     * @return
     */
    public static String addressString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    /**
     * 返回本地地址和端口
     * 
     * @param port
     * @return
     */
    public static InetSocketAddress localAddress(int port) {
        return new InetSocketAddress(LOCAL_HOST, port);
    }

    /**
     * 返回本地地址和端口(默认为0)
     * 
     * @return
     */
    public static InetSocketAddress localAddress() {
        return localAddress(0);
    }

    /**
     * 分割host和端口
     *
     * @param data
     * @return
     * @throws UnknownHostException
     */
    public static InetSocketAddress splitHostAndPortResolved(String data) throws UnknownHostException {
        String[] hostnameAndPort = data.trim().split(":");
        if (hostnameAndPort.length != 2)
            throw new UnknownHostException(data);
        return new InetSocketAddress(hostnameAndPort[0], Integer.parseInt(hostnameAndPort[1]));
    }

    /**
     * 分割host和端口
     * 
     * @param data
     * @return
     * @throws UnknownHostException
     */
    public static InetSocketAddress splitHostAndPort(String data) throws UnknownHostException {
        String[] hostnameAndPort = data.trim().split(":");
        if (hostnameAndPort.length != 2)
            throw new UnknownHostException(data);
        return InetSocketAddress.createUnresolved(hostnameAndPort[0], Integer.parseInt(hostnameAndPort[1]));
    }

    /**
     * 分割host和端口
     * 
     * @param data
     * @return
     */
    public static Set<InetSocketAddress> splitHostAndPort(List<String> data) {
        Set<InetSocketAddress> set = new HashSet<>(data.size());
        for (String s : data) {
            try {
                InetSocketAddress address = splitHostAndPort(s);
                set.add(address);
            } catch (UnknownHostException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return set;
    }

    /**
     * 静默地分割host和端口 即遇到异常时不抛出异常而是返回空指针
     * 
     * @param data
     * @return
     */
    public static InetSocketAddress splitHostAndPortSilently(String data) {
        try {
            return splitHostAndPort(data);
        } catch (UnknownHostException e) {
            LOGGER.error("Error address = " + data + " And it can not be converted");
            return null;
        }
    }

    /**
     * 获取端口号
     * 
     * @param data
     * @return
     */
    public static int getPort(String data) {
        String[] hostnameAndPort = data.trim().split(":");
        return Integer.parseInt(hostnameAndPort[1]);
    }

    /**
     * 获取主机名
     * 
     * @param data
     * @return
     */
    public static String getHost(String data) {
        String[] hostnameAndPort = data.trim().split(":");
        return hostnameAndPort[0];
    }
}