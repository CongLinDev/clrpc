package conglin.clrpc.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContext;

public class ObjectLifecycleUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectLifecycleUtils.class);

    private ObjectLifecycleUtils() {
        // unused
    }

    /**
     * 装配
     *
     * @param object
     */
    public static void assemble(Object object) {
        assemble(object, null);
    }

    /**
     * 装配
     *
     * @param object
     * @param context
     */
    public static void assemble(Object object, ComponentContext context) {
        if (object instanceof ComponentContextAware) {
            ((ComponentContextAware) object).setContext(context);
        }
        if (object instanceof Initializable) {
            ((Initializable) object).init();
        }
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
