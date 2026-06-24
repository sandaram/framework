package servlet;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import util.Util;
import util.Mapping;
import annotation.Controller;
import annotation.UrlMapping;
import java.lang.reflect.Method;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> classesScannees = new ArrayList<>();
    private String packageConfigure = "";
    
    
    private Map<String, Mapping> urlMappingStructure = new HashMap<>();

    @Override
public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
        processRequest(request, response);
    } catch (Exception e) {
        afficherErreurSurNavigateur(response, e);
    }
}

@Override
public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
        processRequest(request, response);
    } catch (Exception e) {
        afficherErreurSurNavigateur(response, e);
    }
}


private void afficherErreurSurNavigateur(HttpServletResponse response, Exception e) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_NOT_FOUND); 
    PrintWriter out = response.getWriter();
    
    out.println("<html><head><title>Erreur de Routage</title></head><body>");
   
    String messageHtml = e.getMessage().replace("\n", "<br/>");
   // out.println("<div style='background-color:#f8d7da; color:#721c24; padding:15px; border-radius:5px; font-family:monospace;'>");
    out.println(messageHtml);
   // out.println("</div>");
    
    out.println("</body></html>");
}

    @Override
    public void init() throws ServletException {
        try {
            packageConfigure = this.getInitParameter("packageToScan");
            if (packageConfigure != null && !packageConfigure.trim().isEmpty()) {
                classesScannees = Util.getClassesWithAnnotation(packageConfigure, Controller.class, Util.NiveauScan.CLASSE);
                
               
                for (Class<?> clazz : classesScannees) {
                    for (Method method : Util.getMethodsWithAnnotation(clazz, UrlMapping.class, Util.NiveauScan.METHODE)) {
                        
                        
                        UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                        String urlAssociee = annotation.url();
                        
                       
                        Mapping mappingInfo = new Mapping(clazz.getName(), method.getName());
                        
                     
                        urlMappingStructure.put(urlAssociee, mappingInfo);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du FrontControllerServlet", e);
        }
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String urlDemandee = requestURI.substring(contextPath.length());

        out.println("<html><body>");
        
        
        if (urlMappingStructure.containsKey(urlDemandee)) {
            Mapping mapping = urlMappingStructure.get(urlDemandee);
            
           
            out.println("<p><b>URL demandée :</b> " + urlDemandee + "</p>");
            out.println("<p><b>Classe cible :</b> " + mapping.getClassName() + "</p>");
            out.println("<p><b>Méthode cible :</b> " + mapping.getMethodName() + "</p>");
            
        } else {
           
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Aucune méthode ne correspond à l'URL : '").append(urlDemandee).append("'.\n");
            errorMsg.append("Listes des URLs associées disponibles :\n");
            
            for (Map.Entry<String, Mapping> entry : urlMappingStructure.entrySet()) {
                errorMsg.append("- URL: ").append(entry.getKey())
                        .append(" -> Classe: ").append(entry.getValue().getClassName())
                        .append(", Méthode: ").append(entry.getValue().getMethodName()).append("\n");
            }
            
           
            throw new Exception(errorMsg.toString());
        }
        
        out.println("</body></html>");
    }   
}