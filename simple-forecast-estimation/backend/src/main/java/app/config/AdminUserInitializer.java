package app.config;

import app.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class AdminUserInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Qualifier("customUserDetailsManager")
    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        User adminUser = new User(null,
                null,
                "admin",
                "Admin User",
                passwordEncoder.encode("admin"),
                null,
                true,
                true,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")),
                true,
                true,
                true,
                true,
                null);

        if (!userDetailsManager.userExists(adminUser.getUsername())) {
            userDetailsManager.createUser(adminUser);
        }
    }
}
