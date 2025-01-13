package pwr.isa.klama.security.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import pwr.isa.klama.user.UserService;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationSuccessHandler customAuthenticationSuccessHandler;


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(customizer ->
                        customizer
                                .requestMatchers("/api/v1/authorized/admin/**").hasAuthority("ROLE_ADMIN") // Only for ADMIN
                                .requestMatchers("/api/v1/authorized/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN") // For USER and ADMIN
                                .requestMatchers("/api/v1/**").permitAll() // Publicly accessible, no login required
                                .requestMatchers("/admin", "/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/home", "/account", "/purchaseHistory","/rentalHistory", "/home/**").hasAuthority("ROLE_USER")
                                .requestMatchers("/login", "/register", "/reset-password", "/confirm").permitAll()
                                .requestMatchers("/", "/index.html", "/static/**", "/resources/**").permitAll() // Allow access to static resources
                                .requestMatchers("/scripts/**", "/styles/**", "/img/**").permitAll() // Allow access to static resources
                                .requestMatchers("/blog").permitAll()
                                .anyRequest()
                                .authenticated()
                ) 
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .formLogin(httpConfig -> httpConfig
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(customAuthenticationSuccessHandler) // Dodano obsługę przekierowania po zalogowaniu
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
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