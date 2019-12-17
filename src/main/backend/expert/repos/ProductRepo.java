package expert.repos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import expert.domain.Product;
import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, String> {
    Product findByProductID(String productID);

    Product findFirstByProductGroupAndPicIsNotNullAndPicNotContains(String group, String not_link);

    List<Product> findBySupplier(String supplier);

    List<Product> findByProductGroupIgnoreCase(String group);

    List<Product> findByProductCategoryIgnoreCase(String category);

    List<Product> findProductsByProductGroupIgnoreCase(String productGroup);

    List<Product> findBySupplierAndShortModelNameIgnoreCase(String supplier, String shortModel);

    Page<Product> findByProductGroupIgnoreCaseOrderByPicAsc(String group, Pageable pageable);

    List<Product> findBySupplierAndPicNotContains(String s, String ss);

    List<Product> findByBrandIgnoreCaseAndPicNotContains(String s, String ss);
}

