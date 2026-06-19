package servlet;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList; 
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import util.Util;
import annotation.Controller; 

public class FrontControllerServlet extends HttpServlet {

   
    private List<Class<?>> classesScannees = new ArrayList<>();
    private String packageConfigure = "";

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void init() throws ServletException {
        try {
           
            packageConfigure = this.getInitParameter("packageToScan");
            
            if (packageConfigure != null && !packageConfigure.trim().isEmpty()) {
              
                classesScannees = Util.getClassesWithAnnotation(packageConfigure, Controller.class, Util.NiveauScan.CLASSE);
                //classesScannees = Util.getClassesInPackage(packageConfigure);
                //Util.getClassesInPackage(packageConfigure);
            }
            
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du FrontControllerServlet", e);
        }
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html><body>");
       
        
        if (classesScannees.isEmpty()) {
            out.println("<p style='color:orange;'>Aucune classe découverte ou package non configuré.</p>");
        } else {
            out.println("<h3>Classes découvertes au démarrage :</h3>");
            out.println("<ul>");
            for (Class<?> clazz : classesScannees) {
                out.println("<li><b>" + clazz.getName() + "</b></li>");
            }
            out.println("</ul>");
        }
        
       
        out.println("</body></html>");
    }   
}