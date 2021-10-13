package conglin.clrpc.service.annotation;

import java.lang.reflect.Method;

public class AnnotationParser {

    private AnnotationParser() {
        // unused
    }

    /**
     * 解析注解 {@link Transaction} 是否是事务方法
     * 
     * @param method
     * @return
     */
    public static boolean isTransactionMethod(Method method) {
        Transaction transaction = method.getAnnotation(Transaction.class);
        return transaction != null;
    }

    /**
     * 解析注解 {@link Transaction} 寻找事务预提交方法
     * 
     * @param method
     * @return
     */
    public static Method precommitMethod(Method method) {
        Transaction transaction = method.getAnnotation(Transaction.class);

        try {
            if (transaction != null) {
                Class<?> clazz = method.getDeclaringClass();
                return clazz.getDeclaredMethod(transaction.precommit(), method.getParameterTypes());
            }
        } catch (NoSuchMethodException e) {
            // do nothing
        }
        return null;
    }
}