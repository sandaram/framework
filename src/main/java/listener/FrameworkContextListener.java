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

        // 🚀 On récupère dynamiquement le nom de la classe de config depuis le web.xml
        String configClassName = context.getInitParameter("springConfigClass");
        if (configClassName == null || configClassName.isEmpty()) {
            throw new IllegalArgumentException("Le paramètre 'springConfigClass' est manquant dans le web.xml !");
        }

        try {
            // On charge la classe par réflexion pour éviter l'import direct
            Class<?> configClass = Class.forName(configClassName);

            // 1. Démarrage de Spring Boot avec la classe chargée dynamiquement
            this.springContext = new SpringApplicationBuilder(configClass)
                    .web(org.springframework.boot.WebApplicationType.NONE)
                    .run();

            // 2. Enregistrement du contexte dans l'utilitaire statique
            SpringContextTenant.setContext(this.springContext);

            // 🚀 LE CHAÎNON MANQUANT : On le partage aussi dans les attributs de la servlet
            context.setAttribute("springContext", this.springContext);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Impossible de trouver la classe de configuration Spring : " + configClassName, e);
        }

        // 3. Scan et initialisation de tes routes de framework habituelles
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