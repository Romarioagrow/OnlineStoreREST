package expert.domain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import expert.dto.OrderedProduct;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log
@Data
@Entity
@Table(name = "ordr")
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    public Order(String sessionID) {
        this.setSessionID(sessionID);
    }

    public Order(User user) {
        this.setUser(user);
    }

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orderID;

    @Column(name = "session_id")
    private String sessionID;

    @Column(name = "user_id")
    private Long userID;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, Integer> orderedProducts = new LinkedHashMap<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "ordered_list", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "order_id"))
    private List<OrderedProduct> orderedList;

    @ManyToOne
    private User user;

    private Boolean accepted = false, confirmed = false, completed = false;

    private Integer totalPrice = 0, totalBonus = 0, discount, discountPrice;

    private String address, clientName, clientMobile;

    private LocalDateTime openDate = LocalDateTime.now().withSecond(0).withNano(0);
}
