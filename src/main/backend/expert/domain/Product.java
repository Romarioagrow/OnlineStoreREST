package expert.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "products")
@NoArgsConstructor
public class Product implements Serializable {
    @Id
    @Column(name = "product_id")
    private String productID;

    private String productCategory, productGroup, productType;

    private Integer finalPrice, bonus, defaultPrice;

    private String brand, supplier;

    private LocalDate updateDate;

    @JsonIgnore
    private Double defaultCoefficient, customCoefficient;

    @Column(length = 1000)
    private String modelName, fullName, singleTypeName, searchName, groupBrandName, shortModelName, originalName;

    @Column(length = 20000)
    private String pic, linkToPic, localPic, pics, annotation, shortAnnotation, formattedAnnotation, annotationFromRUSBT;

    private Boolean priceModified;

    @JsonIgnore
    private Boolean isAvailable, uniquePrice, coefficientModified, hasFormattedAnno, mappedJSON = false;

    public Product(String productID) {
        this.productID = productID;
    }
}
