package ch.systemsx.cisd.openbis.generic.server.security;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.springframework.web.WebApplicationInitializer;

public class EmbeddedKeycloakApplicationInitializer implements WebApplicationInitializer
{

    public void onStartup(ServletContext container) throws ServletException
    {
        EmbeddedKeycloakServerProperties properties = new EmbeddedKeycloakServerProperties();

        ServletRegistration.Dynamic servlet = container.addServlet(
                "keycloakJaxRsApplication", new HttpServlet30Dispatcher());
        servlet.setInitParameter("javax.ws.rs.Application", EmbeddedKeycloakApplication.class.getName());
        servlet.setInitParameter(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, properties.getContextPath());
        servlet.setInitParameter(ResteasyContextParameters.RESTEASY_USE_CONTAINER_FORM_PARAMS, "true");
        servlet.addMapping(properties.getContextPath() + "/*");
        servlet.setLoadOnStartup(1);
        servlet.setAsyncSupported(true);

        FilterRegistration.Dynamic filter =
                container.addFilter("Keycloak Session Management", new EmbeddedKeycloakRequestFilter());
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, properties.getContextPath() + "/*");
    }

}
