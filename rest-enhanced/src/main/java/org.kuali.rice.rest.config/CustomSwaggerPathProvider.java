package org.kuali.rice.rest.config;

import com.mangofactory.swagger.controllers.DefaultSwaggerController;
import com.mangofactory.swagger.paths.RelativeSwaggerPathProvider;
import com.mangofactory.swagger.paths.SwaggerPathProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletContext;

public class CustomSwaggerPathProvider extends RelativeSwaggerPathProvider {

    private final ServletContext servletContext;

    public CustomSwaggerPathProvider(ServletContext servletContext) {
        super(servletContext);
        this.servletContext = servletContext;
    }

    @Override
    protected String applicationPath() {
        return servletContext.getContextPath() + "/rest";
    }

}

