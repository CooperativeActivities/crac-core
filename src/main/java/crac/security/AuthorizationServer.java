package crac.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;

@Configuration
@EnableAuthorizationServer // [1]
class OAuth2Config extends AuthorizationServerConfigurerAdapter {

     @Autowired
     private AuthenticationManager authenticationManager;
     
     private static final String RESOURCE_ID = "crac_resource";

     @Override // [2]
     public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
          endpoints.authenticationManager(authenticationManager);
     }

     @Override // [3]
     public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
          // @formatter:off
          clients.inMemory()
          .withClient("client-with-registered-redirect")
          .authorizedGrantTypes("authorization_code")
          .authorities("ROLE_CLIENT")
          .scopes("read", "trust")
          .resourceIds(RESOURCE_ID)
          .redirectUris("http://localhost:8080/security/access?")
          .secret("secret123")
          .and()
          .withClient("my-client-with-secret")
          .authorizedGrantTypes("client_credentials", "password")
          .authorities("ROLE_CLIENT")
          .scopes("read")
          .resourceIds(RESOURCE_ID)
          .secret("secret");
          // @formatter:on
     } 

}
