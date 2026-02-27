package agendamento.barbearia.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Para facilitar agora (como vocês usam HTML/JS puro), vamos desativar CSRF.
            // Depois, quando subir pra prod, a gente revisa.
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Público (cliente)
                .requestMatchers(
                        "/", "/index.html",
                        "/CSS/**", "/JS/**",
                        "/favicon.ico",
                        "/servicos/**", "/agendamentos/**", "/disponiveis/**",
                        "/ping/**"
                ).permitAll()

                // Se você usa H2 console localmente
                .requestMatchers("/h2-console/**").permitAll()

                // Admin: página e endpoints
                .requestMatchers(
                        "/admin.html",
                        "/admin/**"
                ).hasRole("ADMIN")

                // Qualquer outra coisa: precisa estar logado
                .anyRequest().authenticated()
            )

            // Login padrão do Spring (por enquanto é o suficiente e funciona bem)
            .formLogin(Customizer.withDefaults())

            .logout(logout -> logout.logoutSuccessUrl("/").permitAll())

            // H2 console precisa disso pra abrir em iframe
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

