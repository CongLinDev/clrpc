package conglin.clrpc.common.util;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);

    /**
     * 反射加载给定全限定类名下的类
     * 
     * @param qualifiedClassName
     * @param args
     * @return
     */
    public static Object loadClassObject(String qualifiedClassName, Object... args) {
        return loadClassObject(Object.class, qualifiedClassName, args);
    }

    /**
     * 反射加载指定类型的全限定类名下的类
     * 
     * @param <T>
     * @param superClass
     * @param qualifiedClassName
     * @param args
     * @return
     */
    public static <T> T loadClassObject(Class<T> superClass, String qualifiedClassName, Object... args) {
        try {
            Class<? extends T> clazz = Class.forName(qualifiedClassName).asSubclass(superClass);
            return clazz.getConstructor(getClasses(args)).newInstance(args);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            LOGGER.error("Load class {} error. Cause: {}", qualifiedClassName, e.getMessage());
        }
        return null;
    }

    /**
     * 获取对象数组对应的类型
     * 
     * @param objects
     * @return
     */
    public static Class<?>[] getClasses(Object... objects) {
        Class<?>[] types = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            types[i] = objects[i].getClass();
        }
        return types;
    }
}