package expert.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderedProduct {
    @Id
    String productID;
    String productName, pic;
    Integer productAmount, productPrice;

    public OrderedProduct(String productID, Integer amount, String name, String pic, Integer finalPrice) {
        this.productID = productID;
        this.productAmount = amount;
        this.productName = name;
        this.pic = pic;
        this.productPrice = finalPrice;
    }
}
