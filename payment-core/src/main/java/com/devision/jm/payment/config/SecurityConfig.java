// package com.devision.jm.payment.config;

// import com.devision.jm.payment.filter.InternalApiKeyValidationFilter;
// import lombok.RequiredArgsConstructor;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration
// @RequiredArgsConstructor
// public class SecurityConfig {

//   private final InternalApiKeyValidationFilter internalApiKeyValidationFilter;

//   @Bean
//   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//     return http
//       .csrf(csrf -> csrf.disable())
//       .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//       .authorizeHttpRequests(auth -> auth
//         .requestMatchers("/api/payments/webhooks/**").permitAll()
//         .anyRequest().permitAll()
//       )
//       .addFilterBefore(internalApiKeyValidationFilter, UsernamePasswordAuthenticationFilter.class)
//       .build();
//   }
// }
