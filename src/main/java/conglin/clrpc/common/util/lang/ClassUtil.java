package conglin.clrpc.common.util.lang;

public class ClassUtil{
    public static Class<?> getClassType(Object obj){
        return obj.getClass();
    }

    public static Class<?>[] getClassType(Object[] objs){
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = getClassType(objs[i]);
        }
        return types;
    }
}