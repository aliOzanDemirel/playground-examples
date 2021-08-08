package app.service;

import app.config.security.jwt.InvalidJwtAuthenticationException;
import app.config.security.jwt.JwtTokenProvider;
import app.domain.HighScore;
import app.domain.User;
import app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class UserService {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Qualifier("customUserDetailsManager")
    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String signIn(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsername(username).orElseThrow(() ->
                    new UsernameNotFoundException("Username " + username + " not found"));
            return jwtTokenProvider.createToken(username, user.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).collect(toList()));
        } catch (AuthenticationException e) {
            LOGGER.error("Sign in error.", e);
            throw new InvalidJwtAuthenticationException("Invalid username/password supplied");
        }
    }

    public void signUp(User user) {
        if (!userDetailsManager.userExists(user.getUsername())) {

            final User payload = new User(
                    null,
                    null,
                    user.getUsername(),
                    user.getName(),
                    passwordEncoder.encode(user.getPassword()),
                    null,
                    user.isGdprConfirmed(),
                    user.isCookieConfirmed(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    true,
                    true,
                    true,
                    true,
                    null
            );
            userDetailsManager.createUser(payload);
        } else {
            throw new InvalidJwtAuthenticationException("Username is already in use");
        }
    }

    void newHighScore(String userUUID, double score) {
        User user = userRepository
                .findById(userUUID)
                .orElseThrow(() -> new UsernameNotFoundException("User id " + userUUID + "not found"));

        if (user.getHighScore() == null || user.getHighScore() < score) {
            user.setHighScore(score);
            userRepository.save(user);
        }
    }

    public List<HighScore> getHighScores() {
        return userRepository.findAllByOrderByHighScoreDesc();
    }
}
