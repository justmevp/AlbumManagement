package org.studyeasy.SpringRestDemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private RSAKey rsaKeys;

    // Phương thức này tạo một JWKSource để cung cấp các khóa RSA cho JWT
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        rsaKeys = Jwks.generateRsa(); // Tạo khóa RSA (cặp khóa công khai và riêng tư)
        JWKSet jwkSet = new JWKSet(rsaKeys); // Đưa RSAKey vào JWKSet
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet); // Cung cấp JWKSet cho nguồn khóa
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // // Phương thức này tạo một InMemoryUserDetailsManager để lưu trữ thông tin
    // người dùng trong bộ nhớ
    // @Bean
    // public InMemoryUserDetailsManager users() {
    // return new InMemoryUserDetailsManager(
    // User.withUsername("chaand")
    // .password("{noop}password") // Định nghĩa tài khoản người dùng "chaand" với
    // mật khẩu "password"
    // .authorities("read") // Cấp quyền "read" cho người dùng này
    // .build()
    // );
    // }

    // Phương thức này tạo AuthenticationManager với DaoAuthenticationProvider dựa
    // trên UserDetailsService
    @Bean
    public AuthenticationManager authManager(UserDetailsService userDetailsService) {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService); // Sử dụng UserDetailsService để xác thực
        return new ProviderManager(authProvider); // Trả về AuthenticationManager với DaoAuthenticationProvider
    }

    // Phương thức này tạo JwtDecoder để giải mã các JWT, sử dụng khóa công khai của
    // RSA
    @Bean
    JwtDecoder jwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKeys.toRSAPublicKey()).build();
    }

    // Phương thức này tạo JwtEncoder để mã hóa JWT, sử dụng JWKSource chứa khóa RSA
    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwks) {
        return new NimbusJwtEncoder(jwks);
    }

    // Phương thức này cấu hình SecurityFilterChain cho ứng dụng, thiết lập các quy
    // tắc bảo mật
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http    
                .headers().frameOptions().sameOrigin()
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/**").permitAll()
                .requestMatchers("/api/v1/auth/token").permitAll()
                .requestMatchers("/api/v1/auth/users/add").permitAll()
                .requestMatchers("/api/v1/auth/users").hasAuthority("SCOPE_ADMIN")
                .requestMatchers("/api/v1/auth/users/{user_id}/update-authorities").hasAuthority("SCOPE_ADMIN")
                .requestMatchers("/api/v1/auth/profile").authenticated()
                .requestMatchers("/api/v1/auth/profile/update-password").authenticated()
                .requestMatchers("/api/v1/auth/profile/delete").authenticated()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                
                // TODO: remove these after upgrading the DB from H2 infile DB
              
                http.csrf().disable();
                http.headers().frameOptions().disable();

        return http.build();
    }

}
