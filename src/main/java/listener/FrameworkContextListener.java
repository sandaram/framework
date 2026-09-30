package listener;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import util.*;

public class FrameworkContextListener implements ServletContextListener {

    private ConfigurableApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        
        String configClassName = context.getInitParameter("springConfigClass");
        if (configClassName == null || configClassName.isEmpty()) {
            throw new IllegalArgumentException("Le paramètre 'springConfigClass' est manquant dans le web.xml !");
        }

        try {
            
            Class<?> configClass = Class.forName(configClassName);

           
            this.springContext = new SpringApplicationBuilder(configClass)
                    .web(org.springframework.boot.WebApplicationType.NONE)
                    .run();

          
            SpringContextTenant.setContext(this.springContext);

          
            context.setAttribute("springContext", this.springContext);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Impossible de trouver la classe de configuration Spring : " + configClassName, e);
        }

      
        String packageName = context.getInitParameter("packageToScan");
        Map<UrlMethod, Mapping> routes = new HashMap<>();
        Utils.buildRoutingTable(packageName, routes);

        context.setAttribute("routes", routes);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (this.springContext != null) {
            this.springContext.close();
        }
    }
}