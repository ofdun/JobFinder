package com.ofdun.jobfinder.features.auth.security;

import com.ofdun.jobfinder.features.auth.domain.jwt.JwtProvider;
import com.ofdun.jobfinder.features.auth.enums.AccountType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String ATTR_ACTOR_ID = "actor.id";
    public static final String ATTR_ACTOR_ROLE = "actor.role";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AccountType accountType = jwtProvider.getAccountType(token);
            if (!jwtProvider.validateToken(token, accountType)) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtProvider.getUserId(token);

            var principal = new JobFinderPrincipal(userId, accountType);
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + accountType.name()));

            var authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(ATTR_ACTOR_ID, userId);
            request.setAttribute(ATTR_ACTOR_ROLE, accountType.name().toLowerCase());
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
            request.removeAttribute(ATTR_ACTOR_ID);
            request.removeAttribute(ATTR_ACTOR_ROLE);
        }

        filterChain.doFilter(request, response);
    }
}
