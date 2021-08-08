package app.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userIfKnown = (auth != null) ? auth.getName() : "Unknown";
        log.warn("Access is denied for user {} while accessing {}, {}", userIfKnown, request.getRequestURI(), exc.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}