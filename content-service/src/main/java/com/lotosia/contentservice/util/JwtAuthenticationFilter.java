package com.lotosia.contentservice.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Get user info from headers set by API Gateway
        String userEmail = request.getHeader("X-User-Email");
        String userIdStr = request.getHeader("X-User-Id");
        String userRolesStr = request.getHeader("X-User-Roles");

        if (userEmail != null && !userEmail.isEmpty()) {
            // Parse user roles from header
            List<String> roles = Collections.emptyList();
            if (userRolesStr != null && !userRolesStr.isEmpty()) {
                roles = Arrays.asList(userRolesStr.split(","));
            }

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

            // Set user ID as details if available
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    authToken.setDetails(userId);
                } catch (NumberFormatException e) {
                    // Ignore invalid user ID
                }
            }

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
