package util;

import java.lang.reflect.Method;

public class Mapping {
    private Class<?> controller;
    private Method method;

    public Mapping(Class<?> controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    public Class<?> getController() { return controller; }
    public Method getMethod() { return method; }
}