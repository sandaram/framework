package servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import annotation.ApiRest;
import util.*;

public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Mapping> routes = new HashMap<>();
    private String prefix = "";
    private String suffix = "";

    @Override
    public void init() throws ServletException {
        this.prefix = this.getInitParameter("prefix");
        this.suffix = this.getInitParameter("suffix");
        if (this.prefix == null) this.prefix = "";
        if (this.suffix == null) this.suffix = "";

        Object routesAttr = getServletContext().getAttribute("routes");
        if (routesAttr != null) {
            this.routes = (Map<UrlMethod, Mapping>) routesAttr;
        }
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String url = request.getRequestURI().substring(contextPath.length());

        if (url.endsWith(".jsp") || url.contains("/views/")) {
            RequestDispatcher dispatcher = request.getServletContext().getNamedDispatcher("jsp");
            if (dispatcher != null) {
                dispatcher.forward(request, response);
            } else {
                request.getRequestDispatcher(url).forward(request, response);
            }
            return;
        }

        UrlMethod urlMethod = new UrlMethod(url, request.getMethod());

        if (routes.containsKey(urlMethod)) {
            Mapping mapping = routes.get(urlMethod);

            if (mapping.getMethod().isAnnotationPresent(ApiRest.class)) {
                handleApi(mapping, request, response);
                return;
            }

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            try {
                
              
                Object controllerInstance = mapping.getController().getDeclaredConstructor().newInstance();
    
            SpringContextTenant.autowire(controllerInstance);

                Method method = mapping.getMethod();
                Object[] args = resolveMethodArguments(method, request);

                Object returnValue = method.invoke(controllerInstance, args);

                if (returnValue instanceof ModelView) {
                    ModelView mv = (ModelView) returnValue;

                    HashMap<String, Object> donnees = mv.getData();
                    if (donnees != null) {
                        for (Map.Entry<String, Object> entry : donnees.entrySet()) {
                            request.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }

                    String jspNom = mv.getUrl();
                    if (this.prefix.endsWith("/") && jspNom.startsWith("/")) {
                        jspNom = jspNom.substring(1);
                    }

                    String jspUrlComplete = this.prefix + jspNom + this.suffix;
                    RequestDispatcher dispatcher = request.getRequestDispatcher(jspUrlComplete);
                    dispatcher.forward(request, response);
                    return;
                }

                out.println("<h2>FrontController servlet</h2>");
                out.println("<p><strong>Current URL:</strong> " + request.getRequestURL() + "</p>");
                out.println("<div style='border: 1px solid black; padding: 10px;'>");
                out.println("<p><strong>Controller:</strong> " + mapping.getController().getSimpleName() + "</p>");
                out.println("<p><strong>Method:</strong> " + mapping.getMethod().getName() + "</p>");
                out.println("<p><strong>Return value:</strong> " + returnValue + "</p>");
                out.println("</div>");

            } catch (Exception e) {
                Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                out.println("<p style='color: red;'><strong>Error executing method:</strong> " + cause.getMessage() + "</p>");
                cause.printStackTrace(out);
            }
        } else {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<h2>FrontController servlet</h2>");
            out.println("<p><strong>Current URL:</strong> " + request.getRequestURL() + "</p>");
            out.println("<p style='color: red;'><strong>No matching route found for:</strong> " + url + " [" + request.getMethod() + "]</p>");
            out.println("<p><strong>Available routes:</strong><br/>");

            for (Map.Entry<UrlMethod, Mapping> entry : routes.entrySet()) {
                UrlMethod key = entry.getKey();
                Mapping map = entry.getValue();

                out.println("- " + key.getUrl() + " [" + key.getMethod() + "] -> " +
                        map.getController().getSimpleName() + "." + map.getMethod().getName() + "<br/>");
            }
            out.println("</p>");
        }
    }

    private void handleApi(Mapping mapping, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            
            Object controllerInstance = mapping.getController().getDeclaredConstructor().newInstance();
            SpringContextTenant.autowire(controllerInstance);
            
            Method method = mapping.getMethod();
            Object[] args = resolveMethodArguments(method, request);

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

   
    private Object[] resolveMethodArguments(Method method, HttpServletRequest request) {
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

    
    private Object convertType(String value, Class<?> targetType) {
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}