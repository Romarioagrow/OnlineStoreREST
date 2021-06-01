package expert.dto.popular;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularRepo extends JpaRepository<Popular, String> {
    Popular findByProductID(String productID);
}
