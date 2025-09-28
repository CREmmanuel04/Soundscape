package com.example.soundscape;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
//        PasswordEncoder encoder = passwordEncoder();
//
//        UserDetails admin = User.builder()
//                .username("admin")
//                .password(encoder.encode("password"))
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(admin);
//    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login").permitAll()  // Public pages
                        .requestMatchers("/", "/posts").authenticated() // Protected pages
                        .requestMatchers("/h2-console/**").permitAll()  // Allow the console to work
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Enable custom login page
                        .defaultSuccessUrl("/", true)  // After login, go to the homepage
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/register")  // After
                        .permitAll()
                )
                // Disable CSRF protection for now (for H2 console)
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        return http.build();
    }
}