// package com.devision.jm.payment.config;

// import com.devision.jm.payment.filter.InternalApiKeyValidationFilter;
// import com.devision.jm.payment.filter.JweAuthenticationFilter;
// import lombok.RequiredArgsConstructor;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.MediaType;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.security.web.context.SecurityContextHolderFilter;

// import java.nio.charset.StandardCharsets;

// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final JweAuthenticationFilter jweAuthenticationFilter;
//     private final InternalApiKeyValidationFilter internalApiKeyValidationFilter;

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

//         http
//                 .cors(AbstractHttpConfigurer::disable)
//                 .csrf(AbstractHttpConfigurer::disable)

//                 .sessionManagement(session ->
//                         session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                 )

//                 .exceptionHandling(ex -> ex
//                         .authenticationEntryPoint((req, res, e) -> {
//                             res.setStatus(401);
//                             res.setCharacterEncoding(StandardCharsets.UTF_8.name());
//                             res.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                             res.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
//                         })
//                         .accessDeniedHandler((req, res, e) -> {
//                             res.setStatus(403);
//                             res.setCharacterEncoding(StandardCharsets.UTF_8.name());
//                             res.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                             res.getWriter().write("{\"success\":false,\"message\":\"Forbidden\"}");
//                         })
//                 )

//                 .authorizeHttpRequests(auth -> auth
//                         // .requestMatchers("/", "/actuator/**").permitAll()
//                         // // Nếu bạn có Stripe webhook:
//                         // .requestMatchers("/api/payments/webhook/**").permitAll()

//                         // // Nếu muốn test local không token thì mở tạm:
//                         // // .requestMatchers("/api/payments/checkout").permitAll()

//                         // .anyRequest().authenticated()
//                         .requestMatchers("/", "/actuator/**").permitAll()
//                         .requestMatchers("/api/payments/webhook/**").permitAll()
//                         .requestMatchers("/api/payments/webhooks/**").permitAll()
//                         .requestMatchers("/api/payments/checkout").permitAll()
//                         .anyRequest().authenticated()
//                 )

//                 // 1) Check X-Internal-Api-Key (Gateway) trước
//                 .addFilterBefore(internalApiKeyValidationFilter, SecurityContextHolderFilter.class)

//                 // 2) Check Authorization: Bearer <JWE> sau
//                 .addFilterBefore(jweAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }
// }
