package conglin.clrpc.router.instance;

public class ServiceVersion implements Comparable<ServiceVersion> {

    public final static ServiceVersion DEFAULT_VERSION = new ServiceVersion(1, 0, 0);

    public static ServiceVersion defaultVersion() {
        return DEFAULT_VERSION;
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
        if(getMajor() != o.getMajor()) {
            return getMajor() - o.getMajor();
        }
        if(getMinor() != o.getMinor()) {
            return getMinor() - o.getMinor();
        }
        return getBuild() - o.getBuild();
    }

    @Override
    public String toString() {
        return getMajor() + "." + getMinor() + "." + getBuild();
    }
}
