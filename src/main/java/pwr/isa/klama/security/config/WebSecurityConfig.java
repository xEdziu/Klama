package pwr.isa.klama.security.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import pwr.isa.klama.user.UserService;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(customizer ->
                        customizer
                                .requestMatchers("/api/v1/authorized/admin/**").hasAuthority("ROLE_ADMIN") // Only for ADMIN
                                .requestMatchers("/api/v1/authorized/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN") // For USER and ADMIN
                                .requestMatchers("/api/v1/**").permitAll() // Publicly accessible, no login required
                                .requestMatchers("/admin", "/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/home", "/home/**").hasAuthority("ROLE_USER")
                                .requestMatchers("/login", "/register").permitAll()
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/static/**").permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(httpConfig -> httpConfig
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
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