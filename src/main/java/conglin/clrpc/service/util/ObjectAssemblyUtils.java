package conglin.clrpc.service.util;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;

public class ObjectAssemblyUtils {

    private ObjectAssemblyUtils() {
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
    public static void assemble(Object object, RpcContext context) {
        if (object instanceof ContextAware) {
            ((ContextAware) object).setContext(context);
        }
        if (object instanceof Initializable) {
            ((Initializable) object).init();
        }
    }
}
