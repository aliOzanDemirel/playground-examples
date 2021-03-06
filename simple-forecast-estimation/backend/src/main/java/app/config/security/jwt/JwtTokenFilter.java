package app.config.security.jwt;

import app.controller.UserController;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtTokenFilter extends GenericFilterBean {

    private JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {

        String token = jwtTokenProvider.resolveToken((HttpServletRequest) req);
        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
            } else {
                HttpServletResponse resp = ((HttpServletResponse) res);
                resp.addHeader(HttpHeaders.SET_COOKIE, UserController.getJwtCookie("", 0));
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(req, res);
    }

}
