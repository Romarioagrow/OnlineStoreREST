package expert.domain;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "unique_brands")
public class UniqueBrand {
    @Id
    @Column(name = "product_id")
    public String productID;

    @Column(length = 10000)
    private String annotation;

    private String fullName, brand, originalPrice, finalPrice, percent;
}
