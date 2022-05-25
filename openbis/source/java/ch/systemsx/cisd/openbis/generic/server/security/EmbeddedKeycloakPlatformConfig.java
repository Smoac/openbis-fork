package ch.systemsx.cisd.openbis.generic.server.security;

import org.keycloak.platform.PlatformProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedKeycloakPlatformConfig
{

    @Bean
    protected PlatformProvider simplePlatform()
    {
        return new EmbeddedKeycloakPlatformProvider();
    }

}