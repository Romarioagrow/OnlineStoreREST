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
    public Map<String, TreeSet<String>> params = new TreeMap<>();
    public Map<String, TreeSet<Double>> diapasons = new TreeMap<>();
    public String checklist;

    public void showInfo() {

        System.out.println();
        log.info("Prices:");
        prices.forEach(integer -> log.info(integer + ""));

        System.out.println();
        log.info("Brands:");
        brands.forEach(log::info);

        System.out.println();
        log.info("Features:");
        features.forEach(log::info);

        System.out.println();
        log.info("Params:");
        params.forEach((key, vals) -> log.info(key + ": " + vals));

        System.out.println();
        log.info("Diapasons:");
        diapasons.forEach((key, vals) -> log.info(key + ": " + vals));
    }
}
