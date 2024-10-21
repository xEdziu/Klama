package pwr.isa.klama.security.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pwr.isa.klama.user.UserService;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(customizer ->
                        customizer
                                .requestMatchers(HttpMethod.POST, "/api/v*/register/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v*/register/confirm**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v*/rentalItem/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v*/post/**").permitAll()
                                //temporary for testing
                                .requestMatchers(HttpMethod.POST, "/api/v*/post/**").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/api/v*/post/**").permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/api/v*/post/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v*/user/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v*/user/**").permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(Customizer.withDefaults())
                .authenticationProvider(daoAuthenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }
}
