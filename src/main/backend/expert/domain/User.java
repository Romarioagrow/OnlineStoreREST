package expert.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import expert.domain.categories.Role;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

@Data
@Entity
@Table(name = "usr")
@EqualsAndHashCode(exclude = "roles")

public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long userID;

    @NotBlank(message = "Введите номер телефона!")
    //@Pattern(regexp = "\7(-\\d{3}){2}-\\d{4}")
    private String username;

    @JsonIgnore
    @NotBlank(message = "Введите пароль!")
    private String password;

    @Size(min=2, message = "Минимум 2 символа")
    @NotBlank(message = "Введите имя!")
    private String firstName;

    @Size(min=3, message = "Минимум 3 символа")
    @NotBlank(message = "Введите фамилию!")
    private String lastName;

    private String email, patronymic;

    @Column(name = "pic")
    private String userPic;

    private LocalDateTime registrationDate;

    private boolean isActive;

    private Integer bonus = 0;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    public boolean isAdmin() {
        return roles.contains(Role.ADMIN);
    }

    public boolean isUser() {
        return roles.contains(Role.USER);
    }

    public String getFirstName() {
        return firstName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
