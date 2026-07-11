package listener;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import util.*;

public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext context = sce.getServletContext();

        String packageName = context.getInitParameter("packageToScan");

        Map<UrlMethod, Mapping> routes = new HashMap<>();

        Utils.buildRoutingTable(packageName, routes);

        context.setAttribute("routes", routes);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}