package conglin.clrpc.common.util;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClassUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);

    private ClassUtils() {
        // Unused.
    }

    /**
     * 反射加载给定全限定类名下的类
     * 
     * @param qualifiedClassName
     * @param args
     * @return
     */
    public static Object loadClassObject(String qualifiedClassName, Object... args) {
        return loadClassObject(qualifiedClassName, Object.class, args);
    }

    /**
     * 反射加载指定类型的全限定类名下的类
     * 
     * @param <T>
     * @param qualifiedClassName
     * @param superClass
     * @param args
     * @return
     */
    public static <T> T loadClassObject(String qualifiedClassName, Class<T> superClass, Object... args) {
        if (qualifiedClassName == null)
            return null;
        try {
            Class<? extends T> clazz = Class.forName(qualifiedClassName).asSubclass(superClass);
            Constructor<? extends T> constructor = clazz.getConstructor(getClasses(args));
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            LOGGER.error("Load class {} error. Cause: {}", qualifiedClassName, e.getMessage());
        }
        return null;
    }

    /**
     * 反射调用对象方法
     * 
     * @param object
     * @param methodName
     * @param parameters
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object reflectInvoke(Object object, String methodName, Object... parameters)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Class<?> clazz = object.getClass();
        LOGGER.debug("Invoking class={} method={}", clazz.getName(), methodName);
        Method method = clazz.getMethod(methodName, getClasses(parameters));
        method.setAccessible(true);
        return method.invoke(object, parameters);
    }

    /**
     * 获取对象数组对应的类型
     * 
     * @param objects
     * @return
     */
    public static Class<?>[] getClasses(Object... objects) {
        if (objects == null)
            return new Class<?>[0];
        Class<?>[] types = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            types[i] = objects[i].getClass();
        }
        return types;
    }

    /**
     * 返回类对象的默认值
     * 
     * 除基本类型外(不包括包装类) 均返回 {@code null}
     * 
     * @param clazz
     * @return
     */
    public static Object defaultValue(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return null;
        }
        // 处理基本类型
        if (clazz == boolean.class) {
            return Boolean.valueOf(false);
        } else if (clazz == byte.class) {
            return Byte.valueOf((byte) 0);
        } else if (clazz == char.class) {
            return Character.valueOf((char) 0);
        } else if (clazz == short.class) {
            return Short.valueOf((short) 0);
        } else if (clazz == int.class) {
            return Integer.valueOf(0);
        } else if (clazz == long.class) {
            return Long.valueOf(0);
        } else if (clazz == float.class) {
            return Float.valueOf(0);
        } else if (clazz == double.class) {
            return Double.valueOf(0);
        }

        throw new IllegalArgumentException(clazz.getName());
    }

    /**
     * 默认对象
     * 
     * 针对 {@code interfaceClass} 生成一个代理，只返回默认值。
     * 
     * 如果存在默认方法，则执行默认方法
     * 
     * @param <T>
     * @param interfaceClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T defaultObject(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { interfaceClass }, (proxy, method, args) -> {
                    String methodName = method.getName();
                    if (Object.class == method.getDeclaringClass()) {
                        switch (methodName) {
                            case "equals":
                                return proxy == args[0];
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "toString":
                                return proxy.getClass().getName() + "@"
                                        + Integer.toHexString(System.identityHashCode(proxy))
                                        + ", with Default InvocationHandler";
                            default:
                                throw new IllegalStateException(methodName);
                        }
                    }

                    if (!method.isDefault())
                        return defaultValue(method.getReturnType());
                    // 执行默认方法
                    // return MethodHandles.lookup().in(interfaceClass).unreflectSpecial(method,
                    // interfaceClass).bindTo(proxy).invokeWithArguments(args);
                    Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(interfaceClass).in(interfaceClass)
                            .unreflectSpecial(method, interfaceClass).bindTo(proxy).invokeWithArguments(args);
                });
    }
}