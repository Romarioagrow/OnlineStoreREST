package expert.domain;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "original_products")
public class OriginalProduct {
    @Id
    @Column(name = "product_id")
    private String productID;

    @Column(length = 10000)
    private String originalCategory, originalGroup, originalType, originalName, originalBrand, originalAmount, originalPrice, parsedModelName;
    private String supplier;

    @Column(length = 20000)
    private String originalAnnotation, originalPic, linkToPic, localPic, pics, infoFromRusbt;

    private LocalDate updateDate;

    private Boolean priceModified, coefficientModified, annotationParsed, modelNameParsed;

    private Integer finalPrice, defaultPrice, bonus;
}
