package conglin.clrpc.common.util;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.exception.DestroyFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectUtils.class);

    private ObjectUtils() {
        // Unused.
    }

    /**
     * destroy object
     *
     * @param object
     * @return
     */
    public static boolean destroy(Object object) {
        if (object instanceof Destroyable) {
            return destroy((Destroyable) object);
        }
        return true;
    }

    /**
     * destroy object
     *
     * @param destroyable
     * @return
     */
    public static boolean destroy(Destroyable destroyable) {
        if (destroyable.isDestroyed()) return true;
        try {
            destroyable.destroy();
            return true;
        } catch (DestroyFailedException e) {
            LOGGER.error("destroy failed {}", e.getMessage());
            return false;
        }
    }
}
