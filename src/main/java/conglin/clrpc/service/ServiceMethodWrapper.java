package conglin.clrpc.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ClassUtils;

public class ServiceMethodWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMethodWrapper.class);

    private final Map<String, List<Method>> methodMap;

    public ServiceMethodWrapper(Class<?> interfaceClass) {
        this.methodMap = customMethodMap(interfaceClass);
    }

    /**
     * invoke method
     * 
     * @param object
     * @param methodName
     * @param parameters
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Object invoke(Object object, String methodName, Object[] parameters)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (isCustomMethodName(methodName)) {
            LOGGER.debug("use custom way to invoke request");
            Method matchMethod = findMatchMethod(methodName, parameters);
            if (matchMethod == null)
                throw new NoSuchMethodException(methodName);
            return matchMethod.invoke(object, parameters);
        } else {
            LOGGER.debug("use normal way to invoke request");
            return ClassUtils.reflectInvoke(object, methodName, parameters);
        }
    }

    /**
     * 利用缓存的 {@link Method} 列表快速查找匹配方法
     * 
     * @param methodName
     * @param parameters
     * @return
     */
    private Method findMatchMethod(String methodName, Object[] parameters) {
        List<Method> candidates = this.methodMap.get(methodName);
        if (candidates == null || candidates.isEmpty())
            return null;
        if (candidates.size() == 1)
            return candidates.get(0);

        // find match method
        final Class<?>[] parameterTypes = ClassUtils.getClasses(parameters);
        final int parameterSize = parameterTypes.length;

        findMatchMethodLoop: for (Method candidate : candidates) {
            Class<?>[] candidateParamterTypes = candidate.getParameterTypes();
            for (int i = 0; i < parameterSize; i++) {
                if (!candidateParamterTypes[i].isAssignableFrom(parameterTypes[i]))
                    continue findMatchMethodLoop;
            }
            return candidate;

        }
        return null;
    }

    /**
     * 自定义 {@link Method} map
     * 
     * 降低运行时方法查找时间
     * 
     * @param clazz
     * @return
     */
    private static Map<String, List<Method>> customMethodMap(Class<?> clazz) {
        return Arrays.stream(clazz.getMethods()).peek(method -> method.setAccessible(true))
                .collect(Collectors.groupingBy(ServiceMethodWrapper::customMethodName,
                        Collectors.collectingAndThen(Collectors.toList(), ServiceMethodWrapper::sortMethod)));
    }

    /**
     * 对 {@link Method} 列表排序，排序顺序按照参数类型
     * 
     * @param methods
     * @return
     */
    private static List<Method> sortMethod(List<Method> methods) {
        methods.sort((m1, m2) -> {
            Class<?>[] parameterTypes1 = m1.getParameterTypes();
            Class<?>[] parameterTypes2 = m2.getParameterTypes();
            int diff = 0;
            for (int i = 0; i < parameterTypes1.length; i++) {
                if (!parameterTypes1[i].equals(parameterTypes2[i])) {
                    if (parameterTypes1[i].isAssignableFrom(parameterTypes2[i])) {
                        diff++;
                    } else if (parameterTypes2[i].isAssignableFrom(parameterTypes1[i])) {
                        diff--;
                    } else {
                        return 0;
                    }
                }
            }
            return diff;
        });
        return methods;
    }

    /**
     * 是否是自定义方法名
     * 
     * @param methodName
     * @return
     */
    public static boolean isCustomMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty())
            return false;
        return methodName.charAt(0) == '$';
    }

    /**
     * 自定义方法名
     * 
     * @param method
     * @return
     */
    public static String customMethodName(Method method) {
        return String.format("$%s$%d", method.getName(), method.getParameterCount());
    }
}
