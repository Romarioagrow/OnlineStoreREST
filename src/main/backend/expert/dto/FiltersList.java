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
    public String checklist;
}
