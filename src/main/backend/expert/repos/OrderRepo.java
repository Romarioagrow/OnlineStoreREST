package expert.repos;
import expert.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import expert.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    Order findByOrderID(Long orderID);

    Order findByUserAndAcceptedFalse(User user);

    Order findBySessionIDAndAcceptedFalse(String sessionID);

    List<Order> findAllByAcceptedTrueAndCompletedFalse();

    List<Order> findAllByUserAndCompletedTrue(User user);

    List<Order> findAllByUserAndAcceptedTrueAndCompletedFalse(User user);

    List<Order> findAllByCompletedTrue();

    List<Order> findAllByAcceptedFalse();

    List<Order> findByClientMobileContains(String mobile);

    List<Order> findByClientNameContainsIgnoreCase(String name);
}
