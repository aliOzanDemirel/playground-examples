package app.config.security.jwt;

import app.config.security.CustomUserDetailsService;
import io.jsonwebtoken.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    public static final String AUTH_COOKIE_NAME = "JWT";
    public static final String AUTH = "Authorization";
    public static final String BEARER = "Bearer";

    private final String secretKey = Base64.getEncoder().encodeToString("secret".getBytes());
    //    private String secretKey = "secret";
    private final long validityInMilliseconds = 3600000;

    @Autowired
    private CustomUserDetailsService userDetailsService;

//    @PostConstruct
//    protected void init() {
//        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
//    }

    public String createToken(String username, List<String> roles) {

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(Cookie[] cookies) {
        AtomicReference<String> tokenValue = new AtomicReference<>();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(cookie -> AUTH_COOKIE_NAME.equals(cookie.getName()))
                    .findFirst()
                    .ifPresent(jwtCookie -> {
                        String bearerToken = jwtCookie.getValue();
                        if (Strings.isNotBlank(bearerToken)) {
                            log.info("Found JWT in cookie: {}", bearerToken);
                            tokenValue.set(bearerToken);
                        }
                    });
            return tokenValue.get();
        } else {
            return null;
        }
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = resolveToken(req.getCookies());
        if (bearerToken == null) {
            log.info("No JWT is found in cookies");
            bearerToken = req.getHeader(AUTH);
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                bearerToken = bearerToken.substring(7);
                log.info("Found bearerToken in header: {}", bearerToken);
            }
        }
        return bearerToken;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Could not validate token!", e);
            return false;
            // ResponseStatusExceptionResolver does not set HTTP response status when exception is thrown from filters
            // throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
        }
    }

}
