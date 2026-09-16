package com.taller360.common.security;

import com.taller360.iam.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final SecurityUserFactory securityUserFactory;

  public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, SecurityUserFactory securityUserFactory) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.securityUserFactory = securityUserFactory;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      try {
        Claims claims = jwtService.parse(header.substring(7));
        userRepository.findByIdAndEmpresaId(jwtService.userId(claims), jwtService.companyId(claims)).ifPresent(user -> {
          AuthPrincipal principal = securityUserFactory.from(user, jwtService.branchId(claims));
          SecurityContextHolder.getContext().setAuthentication(
              new UsernamePasswordAuthenticationToken(principal, null, principal.authorities()));
        });
      } catch (RuntimeException ignored) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }
}
