package ee.taltech.arete_testing_service.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Configuration of various beans used by the application.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.sessionManagement()
                .sessionCreationPolicy(
                        SessionCreationPolicy.ALWAYS) // No session will be created or used by spring security
                .and()
                .httpBasic()
                // .anyRequest().authenticated() // protect all other requests
                .and()
                .csrf()
                .disable(); // disable cross site request forgery, as we don't use cookies - otherwise ALL
        // PUT, POST, DELETE will get HTTP 403!
    }
}

