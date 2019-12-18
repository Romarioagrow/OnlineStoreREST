package expert.dto.popular;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "popular")
@NoArgsConstructor
public class Popular {
    @Id
    String productID;

    public Popular(String productID) {
        this.productID = productID;
    }
}
