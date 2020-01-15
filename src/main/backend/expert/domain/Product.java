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

    private String productCategory, productGroup, brand, supplier;

    private Integer finalPrice, bonus, defaultPrice;

    private LocalDate updateDate;

    @JsonIgnore
    private Double defaultCoefficient, customCoefficient;

    @Column(length = 1000)
    private String fullName, modelName, singleTypeName, searchName, groupBrandName, shortModelName, originalName;

    @Column(length = 20000)
    private String pic, linkToPic, localPic, pics, annotation, shortAnnotation, infoFromRUSBT;

    private Boolean priceModified;

    @JsonIgnore
    private Boolean uniquePrice, coefficientModified, annotationParsed, mappedByJson;

    public Product(String productID) {
        this.productID = productID;
    }
}
