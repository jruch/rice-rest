package org.kuali.rice.rest.config;

/**
 * Created by jruch on 3/2/2015.
 */

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.*;
import com.mangofactory.swagger.models.dto.builder.OAuthBuilder;
import com.mangofactory.swagger.paths.RelativeSwaggerPathProvider;
import com.mangofactory.swagger.paths.SwaggerPathProvider;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger
public class RestConfiguration {
    private SpringSwaggerConfig springSwaggerConfig;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }

    @Bean
    public SwaggerSpringMvcPlugin customImplementation() {
        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig).authorizationTypes(authType())
                .pathProvider(new CustomSwaggerPathProvider(servletContext))
                .apiInfo(apiInfo())
                .includePatterns(".*document.*")
                .useDefaultResponseMessages(false);
    }


    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Rice",
                "Operations on Rice data",
                "My Apps API terms of service",
                "rice@kuali.org",
                "My Apps API Licence Type",
                "My Apps API License URL"
        );
        return apiInfo;
    }

    private List<AuthorizationType> authType() {
        List<AuthorizationScope> scopes = new ArrayList<AuthorizationScope>();
        scopes.add(new AuthorizationScope("access", "Read/write groups and members"));

        List<GrantType> grantTypes = new ArrayList<GrantType>();

        ImplicitGrant implicitGrant = new ImplicitGrant(new LoginEndpoint("http://localhost:8080/oauth/token"),
                "access_token");

        grantTypes.add(implicitGrant);

        AuthorizationType oauth = new OAuthBuilder().scopes(scopes).grantTypes(grantTypes).build();

        return Arrays.asList(oauth);

    }

}
