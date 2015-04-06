package org.kuali.rice.rest.config;


import org.kuali.rice.core.api.CoreApiServiceLocator;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of ClientDetails service using a client and secret defined in the configuration file
 */
@Configuration
public class ClientDetailsServiceConfig {

    public static final String CONFIG_CLIENT_ID = "oauth.client.id";
    public static final String CONFIG_CLIENT_SECRET = "oauth.client.secret";
    public static final String CONFIG_SWAGGER_CLIENT_ID = "oauth.swagger.client.id";
    public static final String CONFIG_SWAGGER_CLIENT_SECRET = "oauth.swagger.client.secret";

    @Bean
    public ClientDetailsService clientDetailsService() {
        InMemoryClientDetailsService clientDetailsService = new InMemoryClientDetailsService();
        Map<String, ClientDetails> clientDetails = new HashMap<String, ClientDetails>();

        String clientId = ((ConfigurationService)GlobalResourceLoader.getService("kualiConfigurationService")).getPropertyValueAsString(CONFIG_CLIENT_ID);
        String secret = ((ConfigurationService)GlobalResourceLoader.getService("kualiConfigurationService")).getPropertyValueAsString(CONFIG_CLIENT_SECRET);
        BaseClientDetails client = new BaseClientDetails(clientId, "rice-rest", "access",
                "client_credentials", "ROLE_USER");
        client.setClientSecret(secret);

        clientDetails.put(clientId, client);

        clientId = ((ConfigurationService)GlobalResourceLoader.getService("kualiConfigurationService")).getPropertyValueAsString(CONFIG_SWAGGER_CLIENT_ID);
        secret = ((ConfigurationService)GlobalResourceLoader.getService("kualiConfigurationService")).getPropertyValueAsString(CONFIG_SWAGGER_CLIENT_SECRET);
        BaseClientDetails swaggerClient = new BaseClientDetails(clientId, "rice-rest", "access",
                "password", "ROLE_USER");
        swaggerClient.setClientSecret(secret);

        clientDetails.put(clientId, swaggerClient);

        clientDetailsService.setClientDetailsStore(clientDetails);

        return clientDetailsService;
    }

}
