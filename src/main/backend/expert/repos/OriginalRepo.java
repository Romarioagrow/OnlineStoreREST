package expert.repos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import expert.domain.OriginalProduct;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OriginalRepo extends JpaRepository<OriginalProduct, String> {
    OriginalProduct findByProductID(String productID);

    List<OriginalProduct> findByOriginalCategory(String today);

    List<OriginalProduct> findByOriginalGroup(String today);

    List<OriginalProduct> findByUpdateDate(LocalDate today);

    List<OriginalProduct> findByLinkToPicNotNull();

    //http://rusbt.ru/catalog/3455/118960/

    List<OriginalProduct> findByLinkToPicContains(String link);
}
