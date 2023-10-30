package dev.george.biolink.component;

import dev.george.biolink.exception.ProfileNotFoundException;
import dev.george.biolink.model.Ban;
import dev.george.biolink.repository.BansRepository;
import dev.george.biolink.service.JwtService;
import dev.george.biolink.service.UserProfileDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final BansRepository bansRepository;
    private final JwtService jwtService;
    private final UserProfileDetailsService profileDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Cookie cookie = WebUtils.getCookie(request, "session");

        if (cookie != null) {
            String value = cookie.getValue();
            Integer userId = jwtService.getIdFromToken(value);

            if (userId != null) {
                try {
                    UserDetails details = profileDetailsService.loadUserById(userId);

                    if (bansRepository.findBansByUserId(userId).stream().noneMatch(Ban::isBanActive)) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                        
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                } catch (ProfileNotFoundException exc) {
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
