package conglin.clrpc.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import conglin.clrpc.service.annotation.ServiceMethod;

public class Test {
    public static void main(String[] args) {
        X x = (X) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { X.class },
                new P());

        System.out.print(x.hashCode());

    }
}

class P implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            System.out.println("Object");
            return proxy.hashCode();
        }
        return 0;
    }
}

interface X {
    String hi();
}

class M implements X {

    @Override
    @ServiceMethod(enable = false)
    public String hi() {
        return null;
    }
}