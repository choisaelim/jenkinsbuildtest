package com.example.user.security;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final Environment env;
    private final UserService userService;
    private final AuthenticationManagerBuilder authManagerBuilder;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // @Bean
    // public AuthenticationManager
    // authenticationManager(AuthenticationConfiguration
    // authenticationConfiguration)
    // throws Exception {
    // AuthenticationManager authManager =
    // authenticationConfiguration.getAuthenticationManager();
    // return authManager;
    // }

    // WebSecurityConfigurerAdapter deprecated > SecurityFilterChain을 빈으로 등록해서
    // httpsecurity 구성
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/actuator/**").permitAll();

        http.authorizeRequests().antMatchers("/**")
                .access("hasIpAddress('" + env.getProperty("gateway.ip") + "')")
                .and().addFilter(getAuthenticationFilter());

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();

        return http.build();

    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        // AuthenticationFilter authFilter = new AuthenticationFilter(userService);
        // authManagerBuilder.userDetailsService(userService);
        AuthenticationManager authManager = authManagerBuilder.getOrBuild();

        AuthenticationFilter authFilter = new AuthenticationFilter(authManager, userService, env);
        // authFilter.setAuthenticationManager(authenticationManager);

        return authFilter;
    }

    // @Bean
    // public AuthenticationManager
    // authenticationManager(AuthenticationConfiguration
    // authenticationConfiguration)
    // throws Exception {
    // return authenticationConfiguration.getAuthenticationManager();
    // }

    // @Bean
    // public AuthenticationManager authenticationManager(HttpSecurity http,
    // BCryptPasswordEncoder bCryptPasswordEncoder,
    // UserDetailService userDetailService)
    // throws Exception {
    // return http.getSharedObject(AuthenticationManagerBuilder.class)
    // .userDetailsService(userDetailsService)
    // .passwordEncoder(bCryptPasswordEncoder)
    // .and()
    // .build();
    // }

}
