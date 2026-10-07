package util;

import java.io.IOException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convertit un objet en chaîne JSON à l'aide de Jackson.
     */
    public static String toJson(Object o) {
        if (o == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sérialisation JSON", e);
        }
    }
    public static void handleApi(Mapping mapping, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            
            Object controllerInstance = mapping.getController().getDeclaredConstructor().newInstance();
            SpringContextTenant.autowire(controllerInstance);
            
            Method method = mapping.getMethod();
            Object[] args = Binding.resolveMethodArguments(method, request);

            Object returnValue = method.invoke(controllerInstance, args);

            String json = (returnValue instanceof String)
                    ? (String) returnValue
                    : JsonUtil.toJson(returnValue);
            response.getWriter().print(json);
        } catch (Exception e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"status\":\"error\",\"message\":"
                    + JsonUtil.toJson(String.valueOf(cause.getMessage())) + "}");
        }
    }

}