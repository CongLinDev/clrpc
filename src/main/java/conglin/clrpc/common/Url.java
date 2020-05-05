package conglin.clrpc.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * url 对象
 * 
 * 用于解析 url 各种参数，但不加以判断是否对错
 */
final public class Url {

    private static final Pattern pattern = Pattern.compile("(\\S+)://(\\S+):(\\d+)(\\S*)(?:\\?)(\\S+)");

    private final String url;

    private final String protocol;

    private final String host;

    private final String port;

    private final String path;

    private final String query;

    private final Map<String, String> parameters;

    public Url(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Url can not be null");
        }

        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            throw new IllegalArgumentException("cannot match url pattern");
        }

        this.url = url;
        this.protocol = matcher.group(1);
        this.host = matcher.group(2);
        this.port = matcher.group(3);
        this.path = matcher.group(4).equals("") ? "/" : matcher.group(4);
        this.query = matcher.group(5);

        String[] params = this.query.split("&");
        if (params.length == 0) {
            this.parameters = Collections.emptyMap();
        } else {
            parameters = new HashMap<>(params.length);
            for (String param : params) {
                String[] pair = param.split("=");
                parameters.put(pair[0], pair[1]);
            }
        }
    }

    /**
     * 参数对
     * 
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * 返回参数结果
     * 
     * @param key
     * @return
     */
    public String getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * 返回参数结果
     * 
     * @param key
     * @return
     */
    public String getParameterOrDefault(String key, String value) {
        return parameters.getOrDefault(key, value);
    }

    /**
     * url
     * 
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 协议
     * 
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 主机
     * 
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * 端口号
     * 
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * 地址
     * 
     * @return
     */
    public String getAddress() {
        return host + ":" + port;
    }

    /**
     * 路径
     * 
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * 参数
     * 
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return getUrl();
    }
}