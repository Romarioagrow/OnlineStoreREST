package expert.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import java.util.*;

@Log
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FiltersList {
    public List<Integer> prices = new LinkedList<>();
    public Set<String> brands = new TreeSet<>(), features = new TreeSet<>();
    public Map<String, TreeSet<Double>> diapasonsFilters = new TreeMap<>();
    public Map<String, TreeSet<String>> paramFilters  = new TreeMap<>();
    public Set<String> checklist = new HashSet<>();

    public void showInfo() {
        System.out.println();
        log.info("Prices: " + prices.toString());
        log.info("Brands: " + brands.toString());
        log.info("Features: " + features.toString());
        diapasonsFilters.forEach((key, value) -> log.info("Diapasons: " + key + value.toString()));
        paramFilters.forEach((key, value) -> log.info("Params: " + key + value.toString()));
    }

    public void showDiapasons() {
        diapasonsFilters.forEach((key, value) -> log.info(key + value.toString()));
    }
}
