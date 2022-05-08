package conglin.clrpc.service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import conglin.clrpc.common.util.ClassUtils;

public class ServiceObjectHolder {
    private final Map<String, ServiceObjectWrapper> map;

    public ServiceObjectHolder() {
        this.map = new HashMap<>();
    }

    /**
     * 加入 {@link ServiceObject}
     * 
     * @param serviceObject
     * @return
     */
    public boolean putServiceObject(ServiceObject<?> serviceObject) {
        return map.putIfAbsent(serviceObject.name(), new ServiceObjectWrapper(serviceObject)) == null;
    }

    /**
     * 获取 {@link ServiceObject}
     * 
     * @param serviceName
     * @return
     */
    public ServiceObject<?> getServiceObject(String serviceName) {
        ServiceObjectWrapper wrapper = map.get(serviceName);
        if (wrapper == null)
            return null;
        return wrapper.getServiceObject();
    }

    /**
     * 获取 wrapper
     * 
     * @param serviceName
     * @return
     */
    public ServiceObjectWrapper getServiceObjectWrapper(String serviceName) {
        return map.get(serviceName);
    }

    /**
     * foreach
     * 
     * @param consumer
     */
    public void forEach(Consumer<ServiceObject<?>> consumer) {
        this.map.values().forEach(v -> consumer.accept(v.getServiceObject()));
    }

    public static class ServiceObjectWrapper {
        private final ServiceObject<?> serviceObject;
        private final Map<String, List<Method>> methodMap;

        public ServiceObjectWrapper(ServiceObject<?> serviceObject) {
            this.serviceObject = serviceObject;
            this.methodMap = ServiceObjectWrapper.customMethodMap(serviceObject.interfaceClass());
        }

        /**
         * @return the serviceObject
         */
        public ServiceObject<?> getServiceObject() {
            return serviceObject;
        }

        /**
         * 利用缓存的 {@link Method} 列表快速查找匹配方法
         * 
         * @param methodName
         * @param parameters
         * @return
         */
        public Method findMatchMethod(String methodName, Object[] parameters) {
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
                    .collect(Collectors.groupingBy(ServiceObjectWrapper::customMethodName,
                            Collectors.collectingAndThen(Collectors.toList(), ServiceObjectWrapper::sortMethod)));
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

}
