package app.controller;

import app.domain.Estimation;
import app.domain.HighScore;
import app.domain.User;
import app.service.EstimationService;
import app.service.UserService;
import app.config.security.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Qualifier("customUserDetailsManager")
    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private UserService userService;

    @Autowired
    private EstimationService estimationService;

    public static String getJwtCookie(String token, long age) {
        return ResponseCookie.from(JwtTokenProvider.AUTH_COOKIE_NAME, token)
                .maxAge(age)
                .path("/")
                .httpOnly(false)
                .build()
                .toString();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<User> signin(@RequestBody User user) {
        LOGGER.info("User {} signing in", user.getUsername());

        String jwt = userService.signIn(user.getUsername(), user.getPassword());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(JwtTokenProvider.AUTH, JwtTokenProvider.BEARER + " " + jwt);
        responseHeaders.set(HttpHeaders.SET_COOKIE, getJwtCookie(jwt, ((long) 60) * 60 * 24 * 365));

        User loggedInPrincipal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok().headers(responseHeaders).body(loggedInPrincipal);
    }

    @PostMapping("/sign-up")
    public void signup(@RequestBody User user) {
        LOGGER.info("Signing up user: {}", user);
        userService.signUp(user);
    }

    @GetMapping
    public boolean userExists(@RequestParam("username") String username) {
        LOGGER.info("Request to check if username {} exists", username);
        return userDetailsManager.userExists(username);
    }

    @GetMapping("/highscores")
    public List<HighScore> getHighscoresOfAllUsers() {
        LOGGER.info("Sending highscores");
        return userService.getHighScores();
    }

    @GetMapping("/self")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<User> getLoggedInUser(@AuthenticationPrincipal User user) {
        LOGGER.info("Sending user details of {}", user.getUsername());
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/{id}/estimations")
    @PreAuthorize("hasAuthority('ROLE_USER') and #id == #user.userUUID and #estimation.userUUID == #user.userUUID")
    public String createEstimations(@AuthenticationPrincipal User user,
                                    @PathVariable("id") String id,
                                    @RequestBody @Valid Estimation estimation) {
        LOGGER.info("Saving estimation {} for user {}", estimation, user.getUsername());
        return estimationService.createEstimation(estimation).getEstimationUUID();
    }

    @GetMapping("/{id}/estimations")
    @PreAuthorize("hasAuthority('ROLE_USER') and #userId == #user.userUUID")
    public List<Estimation> getEstimations(@AuthenticationPrincipal User user,
                                           @PathVariable("id") String userId,
                                           @RequestParam(value = "timeseriesId", required = false) String timeseriesId) {
        LOGGER.info("Returning estimations of user {}", user.getUsername());

        if (timeseriesId == null) {
            return estimationService.getEstimationsByUserId(userId);
        }
        return Arrays.asList(estimationService.getEstimationByUserAndTimeseriesId(userId, timeseriesId));
    }

}
