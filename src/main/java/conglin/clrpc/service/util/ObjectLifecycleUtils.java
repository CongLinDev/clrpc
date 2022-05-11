package conglin.clrpc.service.util;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContext;

public class ObjectLifecycleUtils {

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
     */
    public static void destroy(Object object) {
        if (object instanceof Destroyable destroyable) {
            destroyable.destroy();
        }
    }
}
