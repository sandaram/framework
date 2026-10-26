package util;

public class SpringContextTenant {
    private static Object context; 

    public static void setContext(Object ctx) {
        context = ctx;
    }

    public static <T> T getBean(Class<T> beanClass) {
        try {
            
            return (T) context.getClass().getMethod("getBean", Class.class).invoke(context, beanClass);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la récupération du Bean Spring", e);
        }
    }

    
    public static void autowire(Object instance) {
    if (context == null) {
        System.err.println("❌ ERROR: SpringContextTenant.context est NULL ! L'injection ne peut pas avoir lieu pour " + instance.getClass().getName());
        return;
    }
    try {
        Object factory = context.getClass().getMethod("getAutowireCapableBeanFactory").invoke(context);
        factory.getClass().getMethod("autowireBean", Object.class).invoke(factory, instance);
    } catch (Exception e) {
        throw new IllegalStateException("Erreur lors de l'injection Spring dans " + instance.getClass(), e);
    }
}
}
