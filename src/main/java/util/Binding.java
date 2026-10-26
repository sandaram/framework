package util;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Binding {

    public static Object[] resolveMethodArguments(Method method, HttpServletRequest request) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> paramType = param.getType();

            if (isSimpleType(paramType)) {
                // Gestion des types simples : String, Integer, Double, etc.
                String value = request.getParameter(param.getName());
                args[i] = convertType(value, paramType);
            } else {
                // 🚀 Gestion d'un objet complexe (ex: Avion)
                args[i] = bindObject(paramType, request);
            }
        }
        return args;
    }

    private static Object bindObject(Class<?> clazz, HttpServletRequest request) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // Parcourt tous les champs déclarés de la classe (et superclasses si besoin)
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String paramValue = request.getParameter(field.getName());

                if (paramValue != null && !paramValue.trim().isEmpty()) {
                    Object convertedValue = convertType(paramValue, field.getType());
                    field.set(instance, convertedValue);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du binding de l'objet " + clazz.getName(), e);
        }
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.equals(String.class)
                || type.equals(Integer.class) || type.equals(int.class)
                || type.equals(Double.class) || type.equals(double.class)
                || type.equals(Long.class) || type.equals(long.class)
                || type.equals(Boolean.class) || type.equals(boolean.class);
    }

    private static Object convertType(String value, Class<?> targetType) {
        if (value == null || value.trim().isEmpty()) return null;
        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        return null;
    }
}