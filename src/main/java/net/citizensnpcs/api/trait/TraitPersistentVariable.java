package net.citizensnpcs.api.trait;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TraitPersistentVariable {
    String command() default "";

    String flag() default "";

    String msg() default "";

    public static class AnnotationInvocationHandler implements InvocationHandler {
        private final Map<String, Object> values;

        public AnnotationInvocationHandler(Map<String, Object> values) {
            this.values = values;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (values.containsKey(method.getName()))
                return values.get(method.getName());

            if ("toString".equals(method.getName()))
                return "Runtime @" + method.getDeclaringClass().getSimpleName() + values.toString();

            throw new UnsupportedOperationException("Method " + method.getName() + " not supported");
        }

        @SuppressWarnings("unchecked")
        public static <T extends Annotation> T createAnnotationInstance(Class<T> annotationType,
                Map<String, Object> values) {
            return (T) Proxy.newProxyInstance(annotationType.getClassLoader(), new Class[] { annotationType },
                    new AnnotationInvocationHandler(values));
        }
    }

}
