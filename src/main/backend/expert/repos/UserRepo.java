package expert.repos;
import org.springframework.data.jpa.repository.JpaRepository;
import expert.domain.User;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUserID(Long userID);

    User findByUsername(String username);
}
