package app.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Document
public class User implements UserDetails, CredentialsContainer {

    @Id
    private String userUUID;
    private Long sequence;
    @Indexed (unique = true)
    @NotNull
    private String username;
    private String name;
    @NotNull
    @Size (min = 8, message = "Password has to be at least 8 characters")
    private String password;
    private Double highScore;
    private boolean gdprConfirmed;
    private boolean cookieConfirmed;
    @JsonIgnore
    private Set<GrantedAuthority> authorities;
    @JsonIgnore
    private boolean accountNonExpired;
    @JsonIgnore
    private boolean accountNonLocked;
    @JsonIgnore
    private boolean credentialsNonExpired;
    @JsonIgnore
    private boolean enabled;

    @DBRef
    @JsonIgnore
    private List<Estimation> estimations;

    public User() {
    }

    public User(String userUUID,
                Long sequence,
                @NotNull String username,
                @NotNull String name,
                @NotNull String password,
                Double highScore,
                boolean gdprConfirmed,
                boolean cookieConfirmed,
                Set<GrantedAuthority> authorities,
                boolean accountNonExpired,
                boolean accountNonLocked,
                boolean credentialsNonExpired,
                boolean enabled,
                List<Estimation> estimations) {
        this.userUUID = userUUID;
        this.sequence = sequence;
        this.username = username;
        this.name = name;
        this.password = password;
        this.highScore = highScore;
        this.gdprConfirmed = gdprConfirmed;
        this.cookieConfirmed = cookieConfirmed;
        this.authorities = authorities;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.estimations = estimations;
    }

    public String getUserUUID() {
        return userUUID;
    }

    @JsonProperty
    public Long getSequence() {
        return sequence;
    }

    @JsonIgnore
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty
    public Double getHighScore() {
        return highScore;
    }

    @JsonIgnore
    public void setHighScore(Double highScore) {
        this.highScore = highScore;
    }

    public boolean isGdprConfirmed() {
        return gdprConfirmed;
    }

    public boolean isCookieConfirmed() {
        return cookieConfirmed;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    public List<Estimation> getEstimations() {
        return estimations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(password, username, userUUID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return Objects.equals(this.password, other.password) &&
                Objects.equals(this.username, other.username) &&
                Objects.equals(this.userUUID, other.userUUID);
    }

    @Override
    public String toString() {
        return "User{" +
                "userUUID='" + userUUID + '\'' +
                ", sequence=" + sequence +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", highScore=" + highScore +
                ", gdprConfirmed=" + gdprConfirmed +
                ", cookieConfirmed=" + cookieConfirmed +
                ", authorities=" + authorities +
                ", accountNonExpired=" + accountNonExpired +
                ", accountNonLocked=" + accountNonLocked +
                ", credentialsNonExpired=" + credentialsNonExpired +
                ", enabled=" + enabled +
                ", estimations=" + estimations +
                '}';
    }
}