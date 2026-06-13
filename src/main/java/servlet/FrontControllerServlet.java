package servlet;
import java.io.PrintWriter;
import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
public class FrontControllerServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURL().toString();
        PrintWriter out = response.getWriter();
        out.println("Requested Path: " + url);
    }   
    
}
