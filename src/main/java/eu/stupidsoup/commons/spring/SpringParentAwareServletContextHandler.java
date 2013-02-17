package eu.stupidsoup.commons.spring;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@SuppressWarnings("unchecked")
public class SpringParentAwareServletContextHandler extends ServletContextHandler implements ApplicationContextAware {
  private String contextConfigLocation;

  @PostConstruct
  public void afterPropertiesSet() {
    ServletHandler servletHandler = new ServletHandler();
    this.setServletHandler(servletHandler);

    ServletHolder servletHolder = new ServletHolder();
    servletHandler.setServlets(new ServletHolder[] { servletHolder });
    servletHolder.setName("DispatcherServlet");
    servletHolder.setServlet(new DispatcherServlet());
    if (this.contextConfigLocation != null) {
      servletHolder.setInitParameter("contextConfigLocation", this.contextConfigLocation);
    }

    ServletMapping servletMapping = new ServletMapping();
    servletHandler.setServletMappings(new ServletMapping[] { servletMapping });
    servletMapping.setPathSpec("/");
    servletMapping.setServletName("DispatcherServlet");
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    GenericWebApplicationContext webApplicationContext = new GenericWebApplicationContext();
    webApplicationContext.setServletContext(this.getServletContext());
    webApplicationContext.setParent(applicationContext);
    this.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
        webApplicationContext);
    webApplicationContext.refresh();
  }

  public String getContextConfigLocation() {
    return contextConfigLocation;
  }

  public void setContextConfigLocation(String contextConfigLocation) {
    this.contextConfigLocation = contextConfigLocation;
  }
}