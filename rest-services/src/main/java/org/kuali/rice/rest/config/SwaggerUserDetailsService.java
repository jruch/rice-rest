package org.kuali.rice.rest.config;

import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

@Service
public class SwaggerUserDetailsService implements UserDetailsService {

    public static final String CONFIG_SWAGGER_USER = "oauth.swagger.user";
    public static final String CONFIG_SWAGGER_PASS = "oauth.swagger.pass";

    SwaggerUser swaggerUser = new SwaggerUser(
            ((ConfigurationService)GlobalResourceLoader.getService("kualiConfigurationService")).getPropertyValueAsString(CONFIG_SWAGGER_USER),
            ((ConfigurationService) GlobalResourceLoader.getService("kualiConfigurationService")).getPropertyValueAsString(CONFIG_SWAGGER_PASS),
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || !username.equals(swaggerUser.getUsername())) {
            throw new UsernameNotFoundException("Bad username");
        }

        return swaggerUser;
    }

    protected class SwaggerUser extends User {
        public SwaggerUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
            super(username, password, authorities);
        }

        @Override
        public String getPassword() {
            return ((ConfigurationService) GlobalResourceLoader.getService("kualiConfigurationService")).getPropertyValueAsString(CONFIG_SWAGGER_PASS);
        }
    }

}
