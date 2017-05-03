package com.evolutionarylabs.nanofrm;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by cleberzanella on 10/22/16.
 */
public class DynamicProxy {

    public static final IProxyFactory JDK_FACTORY = new IProxyFactory(){

        @Override
        public <T> T proxy(Class<T> interf, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(interf.getClassLoader(), new Class<?>[] { interf }, handler);
        }

    };

    public static final IProxyFactory BYTE_BUDDY_FACTORY = new IProxyFactory() {

        @Override
        public <T> T proxy(Class<T> classOrInterface, InvocationHandler handler) {

            ByteBuddy buddy = new ByteBuddy();

            Class<? extends T> serviceClass =
                    buddy.subclass(classOrInterface)
                            .method(ElementMatchers.any())
                            .intercept(InvocationHandlerAdapter.of(handler))
                            .make()
                            .load(classOrInterface.getClassLoader())
                            .getLoaded();

            try {

                return serviceClass.newInstance();

            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }

        }
    };

    public interface IProxyFactory {

        <T> T proxy(Class<T> classOrInterface, InvocationHandler handler);

    }

    public static class ConsoleWriteHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            String s = "";

            s += method.getDeclaringClass().getName() + "::";
            s += method.getName();
            s += "() : " + method.getReturnType().getName() + "";

            System.out.println(s);

            if(method.getReturnType().isPrimitive()){
                return Mapper.PrimitiveDefaults.getDefaultValue(method.getReturnType());
            }
            return method.getDefaultValue();
        }
    }

    public  static <T> T proxy(Class<T> clazz, InvocationHandler handler, IProxyFactory proxyFactory){
        return proxyFactory.proxy(clazz, handler);
    }

    private DynamicProxy() {}

}
