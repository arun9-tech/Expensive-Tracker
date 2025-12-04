package com.example.expensetracker.security.jwt;

import com.example.expensetracker.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Add @Component to make it a managed Spring Bean
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        logger.debug("Processing request to: {}", requestURI);
        
        // Bypass JWT authentication for /api/auth/** endpoints
        if (requestURI.startsWith("/api/auth")) {
            logger.debug("Skipping auth check for path: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);
            logger.debug("JWT token found: {}", jwt != null ? "[HIDDEN]" : "null");

            if (jwt != null) {
                logger.debug("Validating JWT token");
                boolean isValid = jwtUtils.validateJwtToken(jwt);
                logger.debug("JWT token validation result: {}", isValid);
                
                if (isValid) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("Loading user details for username: {}", username);
                    
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Successfully authenticated user: {}", username);
                } else {
                    logger.warn("JWT token validation failed for request: {}", requestURI);
                }
            } else {
                logger.warn("No JWT token found in request to: {}", requestURI);
            }
        } catch (Exception e) {
            logger.error("Authentication error for request {}: {}", requestURI, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized - " + e.getMessage());
            return;
        }

        // Always continue the filter chain.
        // Spring Security will handle authorization (like .permitAll()) later.
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        logger.debug("Checking shouldNotFilter for path: {}", path);
        boolean shouldSkip = path.startsWith("/api/auth");
        logger.debug("Path {} shouldSkip: {}", path, shouldSkip);
        return shouldSkip;
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}