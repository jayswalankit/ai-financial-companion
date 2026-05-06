package com.aifinance.financialcompanion.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFiter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFiter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizedHeader = request.getHeader(AUTHORIZATION_HEADER);

        if(authorizedHeader == null || !authorizedHeader.startsWith(BEARER_PREFIX)){
            filterChain.doFilter(request,response);
            return ;
        }

        String token = authorizedHeader.substring(BEARER_PREFIX.length());
        try {
            String username = jwtService.extractUsername(token);
             log.debug("Extract username from the token : {}", username);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(  // Yeh ek "ID Card" jaisa object hai jo Spring Security ko batata hai — "Yeh user authenticated hai, inki yeh details hain, aur inke yeh permissions hain"
                                    userDetails, // kaun hai
                                    null,       // password kya hai
                                    userDetails.getAuthorities() // kya kya kar sakta hai
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else {
                    log.warn("Invalid JWT token for user: {}", username);
                }

            }

        } catch (RuntimeException e) {
            log.error("JWT authentication failed: {}", e.getMessage(), e);
        }
        filterChain.doFilter(request,response);
    }
}
