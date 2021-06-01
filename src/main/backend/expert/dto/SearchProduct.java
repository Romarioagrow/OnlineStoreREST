package expert.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchProduct {
    String productID, fullName, singleName, pic, category, group;
    Integer finalPrice;
}
