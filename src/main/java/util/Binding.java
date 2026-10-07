package util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;

public class Binding {
    public static  Object[] resolveMethodArguments(Method method, HttpServletRequest request) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String paramName = param.getName();
            String requestValue = request.getParameter(paramName);

            if (requestValue != null && !requestValue.trim().isEmpty()) {
                args[i] = convertType(requestValue, param.getType());
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private static Object convertType(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        }
        return value;
    }
}
