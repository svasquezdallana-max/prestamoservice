package com.santacruz.prestamoservice.security;

import com.santacruz.prestamoservice.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class FiltroJwt extends OncePerRequestFilter {

    private final JwtService jwtService;

    public FiltroJwt(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        // El cliente debe enviar: Authorization: Bearer <token>
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            String token = cabecera.substring(7).trim();

            try {
                String email = jwtService.obtenerEmail(token);
                String rol = jwtService.obtenerRol(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                    var autenticacion = new UsernamePasswordAuthenticationToken(email, null, autoridades);
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // A partir de aquí el controller puede leer authentication.getName() = email.
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Token alterado, vencido o mal escrito: no se autentica.
                // Spring Security responderá 401 mediante ManejadorAutenticacion.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
