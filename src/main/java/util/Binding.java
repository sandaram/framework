package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Binding {

    public static Object[] resolveMethodArguments(Method method, HttpServletRequest request) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> paramType = param.getType();
            String paramName = param.getName();

            // Si le compilateur n'a pas conservé les vrais noms des paramètres (-parameters)
            if (paramName.startsWith("arg")) {
                paramName = paramType.getSimpleName().toLowerCase();
            }

            if (isSimpleType(paramType)) {
                // Type simple direct (ex: String nom, int id)
                String value = request.getParameter(paramName);
                args[i] = convertType(value, paramType);

            } else if (paramType.equals(MaSession.class)) {
                // 🚀 Injection automatique de la session personnalisée MaSession
                args[i] = MaSession.from(request.getSession(true));

            } else if (List.class.isAssignableFrom(paramType)) {
                // Liste en argument de méthode d'action (ex: List<Avion> avions)
                Class<?> itemClass = getGenericListType(param);
                args[i] = bindList(paramName, itemClass, request);

            } else {
                // Objet complexe unique ou multiple (ex: Avion, Pilote)
                args[i] = bindObject(paramType, paramName, request);
            }
        }
        return args;
    }

    public static Object bindObject(Class<?> clazz, String prefix, HttpServletRequest request) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            boolean hasData = false;

            // Vérification si la requête est multipart/form-data (pour les fichiers)
            boolean isMultipart = request.getContentType() != null 
                    && request.getContentType().toLowerCase().startsWith("multipart/form-data");

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                String fieldName = field.getName();
                String fullParamName = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

                // 1. CAS D'UNE LISTE D'OBJETS IMBRIQUÉE (ex: List<Diplome> diplomes)
                if (List.class.isAssignableFrom(fieldType)) {
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType pt) {
                        Class<?> genericClass = (Class<?>) pt.getActualTypeArguments()[0];

                        List<Object> list = new ArrayList<>();
                        int index = 0;

                        while (true) {
                            String indexedPrefix = fullParamName + "[" + index + "]";
                            
                            // Binder chaque élément de la liste avec son index
                            Object item = bindObject(genericClass, indexedPrefix, request);

                            if (item == null) {
                                break; // Plus d'élément trouvé à cet index
                            }

                            list.add(item);
                            index++;
                        }

                        if (!list.isEmpty()) {
                            field.set(instance, list);
                            hasData = true;
                        }
                    }
                }
                // 2. CAS DU FICHIER UPLOAD (MapFile)
                else if (fieldType.equals(MapFile.class)) {
                    if (isMultipart) {
                        MapFile mapFile = extractMapFile(request, fullParamName);
                        if (mapFile != null) {
                            field.set(instance, mapFile);
                            hasData = true;
                        }
                    }
                }
                // 3. CAS DES TYPES SIMPLES (String, Integer, int, Double...)
                else if (isSimpleType(fieldType)) {
                    String paramValue = request.getParameter(fullParamName);
                    if (paramValue != null && !paramValue.trim().isEmpty()) {
                        field.set(instance, convertType(paramValue, fieldType));
                        hasData = true;
                    }
                }
                // 4. CAS D'UN OBJET COMPLEXE IMBRIQUÉ (ex: Sexe, Avion, Pilote)
                else {
                    Object nestedInstance = bindObject(fieldType, fullParamName, request);
                    if (nestedInstance != null) {
                        field.set(instance, nestedInstance);
                        hasData = true;
                    }
                }
            }
            return hasData ? instance : null;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du binding de " + clazz.getName(), e);
        }
    }

    /** Extrait le fichier d'une requête multipart et construit l'objet MapFile */
    private static MapFile extractMapFile(HttpServletRequest request, String paramName) {
        try {
            Part part = request.getPart(paramName);
            if (part != null && part.getSize() > 0) {
                String fileName = getSubmittedFileName(part);
                String extension = "";
                if (fileName != null && fileName.contains(".")) {
                    extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                }
                byte[] bytes = part.getInputStream().readAllBytes();
                return new MapFile(fileName, extension, bytes);
            }
        } catch (Exception e) {
            // Fichier absent ou champ inexistant dans la requête
        }
        return null;
    }

    /** Récupère le nom originel du fichier transmis dans le Part */
    private static String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            for (String token : contentDisp.split(";")) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return "file_" + System.currentTimeMillis();
    }

    /** Binding d'une liste passée en paramètre de méthode (ex: avions[0].nom) */
    private static List<Object> bindList(String listParamName, Class<?> itemClass, HttpServletRequest request) {
        List<Object> list = new ArrayList<>();
        int index = 0;

        while (true) {
            String itemPrefix = listParamName + "[" + index + "]";

            boolean hasParamForIndex = false;
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                if (paramNames.nextElement().startsWith(itemPrefix + ".")) {
                    hasParamForIndex = true;
                    break;
                }
            }

            if (!hasParamForIndex) {
                break;
            }

            Object item = bindObject(itemClass, itemPrefix, request);
            if (item != null) {
                list.add(item);
            }
            index++;
        }

        return list;
    }

    /** Extrait la classe générique de List<T> */
    private static Class<?> getGenericListType(Parameter parameter) {
        Type type = parameter.getParameterizedType();
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length > 0 && args[0] instanceof Class<?> clazz) {
                return clazz;
            }
        }
        return Object.class;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.equals(String.class)
                || type.equals(Integer.class) || type.equals(int.class)
                || type.equals(Double.class) || type.equals(double.class)
                || type.equals(Long.class) || type.equals(long.class)
                || type.equals(Float.class) || type.equals(float.class)
                || type.equals(Boolean.class) || type.equals(boolean.class);
    }

    private static Object convertType(String value, Class<?> targetType) {
        if (value == null || value.trim().isEmpty()) return null;
        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == Float.class || targetType == float.class) return Float.parseFloat(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        return null;
    }
}