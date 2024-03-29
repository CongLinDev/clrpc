package conglin.clrpc.service;

final public class ServiceVersion implements Comparable<ServiceVersion> {

    private final static String VERSION_FORMATTER = "%d.%d.%d";
    private final static ServiceVersion DEFAULT_VERSION = new ServiceVersion(1, 0, 0);

    /**
     * 默认版本号
     * 
     * @return
     */
    public static ServiceVersion defaultVersion() {
        return DEFAULT_VERSION;
    }

    /**
     * 解析
     * 
     * @param content
     * @return
     */
    public static ServiceVersion parse(String content) {
        String[] s = content.split("\\.");
        if (s.length != 3) {
            throw new IllegalArgumentException(content);
        }
        return new ServiceVersion(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
    }

    private final int major;

    private final int minor;

    private final int build;

    /**
     * 服务版本 major.minor.build
     *
     * @param major major
     * @param minor minor
     * @param build build
     */
    public ServiceVersion(int major, int minor, int build) {
        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    /**
     * get major
     * 
     * @return
     */
    public int getMajor() {
        return major;
    }

    /**
     * get minor
     * 
     * @return
     */
    public int getMinor() {
        return minor;
    }

    /**
     * get build
     * 
     * @return
     */
    public int getBuild() {
        return build;
    }

    @Override
    public int compareTo(ServiceVersion o) {
        if (getMajor() != o.getMajor()) {
            return getMajor() - o.getMajor();
        }
        if (getMinor() != o.getMinor()) {
            return getMinor() - o.getMinor();
        }
        return getBuild() - o.getBuild();
    }

    @Override
    public String toString() {
        return String.format(VERSION_FORMATTER, getMajor(), getMinor(), getBuild());
    }
}
