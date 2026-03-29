package com.chaoxingweb.auth.config;

import com.chaoxingweb.auth.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1. 从请求中获取 JWT token
            String jwt = getJwtFromRequest(request);

            // 2. 验证 token 并设置认证信息
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validate(jwt)) {
                String username = jwtTokenProvider.getUsername(jwt);

                // 3. 构建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                // 4. 设置认证详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. 设置到 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("设置用户认证: {}", username);
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证: {}", ex.getMessage());
        }

        // 6. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取 JWT token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
