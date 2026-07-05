package util;

import java.lang.reflect.Method;

public class Mapping {
    private Class<?> classType;
    private Method method;

    public Mapping(Class<?> classType, Method method) {
        this.classType = classType;
        this.method = method;
    }

    public Class<?> getClassType() { return classType; }
    public Method getMethod() { return method; }
}