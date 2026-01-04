package com.lotosia.identityservice.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userEmail = request.getHeader("X-User-Email");
        String userIdStr = request.getHeader("X-User-Id");
        String userRoles = request.getHeader("X-User-Roles");

        if (userEmail != null && !userEmail.isEmpty()) {
            try {
                Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
                List<SimpleGrantedAuthority> authorities = Arrays.asList();

                if (userRoles != null && !userRoles.isEmpty()) {
                    authorities = Arrays.stream(userRoles.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userEmail, null, authorities);
                authToken.setDetails(userId);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid authentication data\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}