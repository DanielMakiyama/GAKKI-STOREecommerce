package com.gakki.store.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TratadorDeErroDeSeguranca tratadorDeErro;

    @Value("${gakki.cors.allowed-origins}")
    private String origensPermitidas;

    /** RNF0033 — senha armazenada com BCrypt, nunca em texto claro. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposto para o AuthService usar no login.
     *
     * <p>Delegar ao AuthenticationManager em vez de comparar a senha na
     * mão traz de graça a recusa de conta inativada: o Spring Security
     * consulta {@code isEnabled()} e lança DisabledException (RF0023).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Sem CSRF porque não há cookie de sessão: o token vai no
                // cabeçalho Authorization, que o navegador não envia
                // sozinho em requisição de outro site.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(erro -> erro
                        .authenticationEntryPoint(tratadorDeErro)
                        .accessDeniedHandler(tratadorDeErro))
                .authorizeHttpRequests(autorizacao -> autorizacao
                        // Os caminhos aqui são relativos ao context-path
                        // (/api/v1), que o container retira antes da
                        // comparação.
                        .requestMatchers("/auth/registrar", "/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bandeiras").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // A autorização fina de cada endpoint fica no
                        // @PreAuthorize do controller, ao lado do método
                        // que ela protege.
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();
        // Origens declaradas, não "*": o front roda em localhost:5173 e o
        // backend em 8080, e liberar qualquer origem deixaria qualquer
        // site chamar esta API com o token do usuário.
        configuracao.setAllowedOrigins(Arrays.stream(origensPermitidas.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracao.setExposedHeaders(List.of("Location"));
        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }
}
