package conglin.clrpc.common.object;

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
final public class UrlScheme {

    private static final Pattern pattern = Pattern.compile("(\\S+)://(\\S+):(\\d+)([/a-zA-Z0-9]*)(?:\\?([^\\?]*))?");

    private final String url;

    private final String protocol;

    private final String host;

    private final String port;

    private final String path;

    private final String query;

    private final Map<String, String> parameters;

    /**
     * 解析参数
     *
     * @param parameterString
     * @param paramsSplitRegex
     * @param keyValueSplitRegex
     * @return
     */
    public static Map<String, String> resolveParameters(String parameterString, String paramsSplitRegex,
            String keyValueSplitRegex) {
        if (parameterString == null || parameterString.isEmpty()) {
            return Collections.emptyMap();
        }

        String[] params = parameterString.split(paramsSplitRegex);
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] pair = param.split(keyValueSplitRegex);
            if (pair.length < 2)
                continue;
            map.put(pair[0], pair[1]);
        }
        return map;
    }

    /**
     * 转换参数
     *
     * @param parameters
     * @param paramsJoin
     * @param keyValueJoin
     * @return
     */
    public static String toParameters(Map<String, String> parameters, String paramsJoin, String keyValueJoin) {
        if (parameters.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.append(entry.getKey())
                    .append(keyValueJoin)
                    .append(entry.getValue() == null ? "" : entry.getValue())
                    .append(paramsJoin);
        }
        return builder.substring(0, builder.length() - paramsJoin.length());
    }

    /**
     * 解析scheme
     *
     * 例如：http://1.1.1.1:1111/path0/path1?id=1&name=2
     *
     * url = "http://1.1.1.1:1111/path0/path1?id=1&name=2"
     * protocol = "http"
     * host = "1.1.1.1"
     * port = "1111"
     * path = "/path0/path1"
     * query = "id=1&name=2"
     * parameters = "id"->"1", "name"->"2"
     *
     * @param url
     */
    public UrlScheme(String url) {
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
        this.path = matcher.group(4).isEmpty() ? "/" : matcher.group(4);
        this.query = matcher.group(5) == null ? "" : matcher.group(5);
        this.parameters = Collections.unmodifiableMap(resolveParameters(query, "[&]", "[=]"));
    }

    public UrlScheme(UrlScheme urlScheme) {
        this.url = urlScheme.getUrl();
        this.protocol = urlScheme.getProtocol();
        this.host = urlScheme.getHost();
        this.port = urlScheme.getPort();
        this.path = urlScheme.getPath();
        this.query = urlScheme.getQuery();
        this.parameters = urlScheme.getParameters();
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof UrlScheme urlScheme) {
            return getUrl().equals(urlScheme.getUrl());
        }
        return false;
    }
}