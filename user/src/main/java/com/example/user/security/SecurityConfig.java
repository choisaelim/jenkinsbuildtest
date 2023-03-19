package com.example.user.security;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.user.security.jwt.AuthEntryPointJwt;
import com.example.user.security.jwt.AuthTokenFilter;
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

    private final AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return authProvider;
    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // WebSecurityConfigurerAdapter deprecated > SecurityFilterChain을 빈으로 등록해서
    // httpsecurity 구성
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // http.csrf().disable();
        // http.authorizeRequests().antMatchers("/actuator/**").permitAll();

        // http.authorizeRequests().antMatchers("/**")
        // .access("hasIpAddress('" + env.getProperty("gateway.ip") + "')")
        // .and().addFilter(getAuthenticationFilter());

        // http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // http.headers().frameOptions().disable();

        // return http.build();

        http.csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                // .antMatchers("/user-service/login").permitAll()
                .antMatchers("/actuator/**").permitAll();

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // private AuthenticationFilter getAuthenticationFilter() throws Exception {
    // AuthenticationManager authManager = authManagerBuilder.getOrBuild();

    // AuthenticationFilter authFilter = new AuthenticationFilter(authManager,
    // userService, env);

    // return authFilter;
    // }
}
