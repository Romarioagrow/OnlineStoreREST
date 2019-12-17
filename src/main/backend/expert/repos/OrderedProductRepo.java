package expert.repos;

import expert.dto.OrderedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderedProductRepo extends JpaRepository<OrderedProduct, String> {
}
