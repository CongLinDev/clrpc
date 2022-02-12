package conglin.clrpc.service;

final public class ServiceVersion implements Comparable<ServiceVersion> {

    private final static ServiceVersion DEFAULT_VERSION = new ServiceVersion(1, 0, 0);

    public static ServiceVersion defaultVersion() {
        return DEFAULT_VERSION;
    }

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

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

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
        return getMajor() + "." + getMinor() + "." + getBuild();
    }
}
