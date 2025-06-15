package com.project.citasalud.jwt;

import com.project.citasalud.tokenJWT.Token;
import com.project.citasalud.tokenJWT.TokenRepository;
import com.project.citasalud.user.User;
import com.project.citasalud.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isAuthRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = extractToken(request);
        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String userDni = jwtService.getDniFromToken(jwtToken);
        if (userDni == null || isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isTokenValid(jwtToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> user = userRepository.findByDni(userDni);
        if (user.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        authenticateUser(userDni, request);
        filterChain.doFilter(request, response);
    }

    private boolean isAuthRequest(HttpServletRequest request) {
        return request.getServletPath().contains("/auth");
    }

    private String extractToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private boolean isTokenValid(String jwtToken) {
        Optional<Token> token = tokenRepository.findByToken(jwtToken);
        return token.isPresent() && !token.get().isExpired() && !token.get().isRevoked();
    }

    private void authenticateUser(String userDni, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userDni);
        if (!jwtService.isTokenValid(extractToken(request), userDetails)) {
            return;
        }

        var authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }


}
