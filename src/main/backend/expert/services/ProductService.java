package expert.services;

import com.google.common.collect.Iterables;
import expert.domain.Product;
import expert.dto.FiltersList;
import expert.dto.OrderedProduct;
import expert.dto.popular.Popular;
import expert.dto.popular.PopularRepo;
import expert.repos.OrderedProductRepo;
import expert.repos.ProductRepo;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;

@Log
@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepo productRepo;
    private final OrderedProductRepo orderedProductRepo;
    private final PopularRepo popularRepo;

    /*Создать фильтры для группы products*/
    public FiltersList createProductsFilterLists(String group) {
        FiltersList filtersList = new FiltersList();

        /*Наполнение списка товаров нужной группы*/
        List<Product> products = productRepo.findProductsByProductGroupIgnoreCase(group);

        /*Checklist для active/disabled фильтров текущей модели на клиенте*/
        filtersList.checklist = createDefaultFilterChecklist(group);

        Map<String, TreeSet<String>> filtersMap = new TreeMap<>();

        /*Сформировать обрабатываемые фильтры*/
        products.forEach(product ->
        {
            if (product.getSupplier().equals("RBT") || product.getAnnotationParsed() != null) {
                String annotation = product.getAnnotation();

                /*Разбиение аннотации экземпляра Product на фильтры*/
                String[] filters = annotation.split(";");

                /*Итерация и отсев неподходящих под фильтры-особенности*/
                for (String filter : filters)
                {
                    String annoKey = substringBefore(filter, ":").trim();
                    String annoVal = substringAfter(filter, ":").trim();

                    if (filterIsValid(annoKey, annoVal)) {
                        filtersMap.computeIfAbsent(annoKey, val -> new TreeSet<>()).add(annoVal);
                    }
                }
            }
        });

        filtersList.prices = collectPriceFilters(filtersList.prices, products);

        filtersList.brands = collectBrandFilters(filtersList.brands, products);

        filtersList.params = collectParamFilters(filtersList.params, filtersMap);

        filtersList.features = collectFeatureFilters(filtersList.features, filtersMap);

        filtersList.diapasons = collectDiapasonFilters(filtersList.diapasons, filtersMap);

        /*System.out.println();
        System.out.println();
        System.out.println();
        filtersList.showInfo();*/
        return filtersList;
    }

    /*Основной алгоритм фильтрации*/
    public LinkedList<Object> filterProducts(LinkedHashMap<String, String> filters, String group, Pageable pageable) {
        /*
        switch(filterKey) {
            case "Smart TV" {
                if group.equals"TV" return SmartTV
            }
        }
        */

        List<Product> products = productRepo.findByProductGroupIgnoreCase(group);

        for (Map.Entry<String, String> filterPair : filters.entrySet()) {
            products = filteringProducts(products, filterPair);
        }

        String filterCheckList = createComputedFilterChecklist(products);
        return productsPage(products, pageable, filterCheckList);
    }

    /*Сама фильтрация*/
    private List<Product> filteringProducts(List<Product> products, Map.Entry<String, String> filtersPair) {
        String filterKey = substringBefore(filtersPair.getKey(), ";");
        String filterVal = extractFilterValue(filtersPair);

        log.info(filterKey);
        log.info(filterVal);

        switch (filterKey) {
            case "price": return filterProductsByPrice(products, filterVal);
            case "brand": return filterProductsByBrands(products, filterVal);
            case "param": return filterProductsByParams(products, filterVal);
            case "feature": return filterProductsByFeature(products, filterVal);
            case "diapason": return filterProductsByDiapasons(products, filterVal);
            default: return products;
        }
    }

    private String extractFilterValue(Map.Entry<String, String> filtersPair) {
        String filterKey = filtersPair.getKey();
        String filterVal = filtersPair.getValue();

        if (filterKey.startsWith("price")) {
            return filterVal;
        }

        if (filterKey.startsWith("feature") || filterKey.startsWith("param") || filterKey.startsWith("brand")) {
            return substringAfter(filterKey, ";");
        }

        if (filterKey.startsWith("diapason")) {
            return substringAfter(filterKey, ";").concat(":").concat(filterVal);
        }
        return null;
    }

    private List<Product> filterProductsByFeature(List<Product> products, String filterName) {
        final String filter1 = filterName.toLowerCase().concat(": да");
        final String filter2 = filterName.toLowerCase().concat(": есть");

        return products.stream().filter(product ->
        {
            String annotation = product.getShortAnnotation().toLowerCase();
            return annotation.contains(filter1) || annotation.contains(filter2);
        }).collect(Collectors.toList());
    }

    private List<Product> filterProductsByParams(List<Product> products, String filterName) {
        return products.stream().filter(product -> containsIgnoreCase(product.getShortAnnotation(), filterName)).collect(Collectors.toList());
    }

    private List<Product> filterProductsByBrands(List<Product> products, String filterName) {
        return products.stream().filter(product -> containsIgnoreCase(product.getBrand(), filterName)).collect(Collectors.toList());
    }

    private List<Product> filterProductsByPrice(List<Product> products, String filterName) {
        return products.stream().filter(product -> {
            int minPrice = Integer.parseInt(substringBefore(filterName, ","));
            int maxPrice = Integer.parseInt(substringAfter(filterName, ","));
            return product.getFinalPrice() >= minPrice && product.getFinalPrice() <= maxPrice;
        }).collect(Collectors.toList());
    }

    private List<Product> filterProductsByDiapasons(List<Product> products, String filterName) {
        return products.stream().filter(product ->
        {
            String annotation = product.getShortAnnotation();
            String diapasonKey = substringBefore(filterName, ":");

            if (annotation.contains(diapasonKey))
            {
                try
                {
                    String checkingFilter = substringBetween(annotation, diapasonKey, ";");
                    Double minFilter = Double.parseDouble(substringBetween(filterName, ":", ","));
                    Double maxFilter = Double.parseDouble(substringAfter(filterName, ","));
                    double checkingValue = Double.parseDouble(substringAfter(checkingFilter, ":").replaceAll(",","."));

                    if (minFilter == null || maxFilter == null) return false;

                    return checkingValue >= minFilter && checkingValue <= maxFilter;
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    private LinkedList<Object> productsPage(List<Product> products, Pageable pageable, String filterCheckList) {
        try{
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), products.size());
            Page<Product> page = new PageImpl<>(products.subList(start, end), pageable, products.size());

            LinkedList<Object> payload = new LinkedList<>();
            payload.add(page);
            payload.add(filterCheckList);
            return payload;
        }
        catch (IllegalArgumentException e) {
            e.getStackTrace();
        }
        return null;
    }

    public List<Product> searchProducts(String searchRequest) {
        List<Product> products =  productRepo.findAll();

        if (!searchRequest.contains(" ")) {

            products = products.stream()
                    .filter(product -> containsIgnoreCase(product.getSearchName(), searchRequest))
                    .limit(10)
                    .collect(Collectors.toList());
        }
        else
        {
            String[] requests = searchRequest.split(" ");
            for (String request : requests) {
                products = products.stream()
                        .filter(product -> containsIgnoreCase(product.getSearchName(), request))
                        .collect(Collectors.toList());
            }
            products = products.stream().limit(10).collect(Collectors.toList());
        }
        return products;
    }

    private Map<String, TreeSet<Double>> collectDiapasonFilters(Map<String, TreeSet<Double>> diapasons, Map<String, TreeSet<String>> filtersMap) {
        /*!!! Если последнее значение меньше 10, то в параметры*/
        /*ЕСЛИ ФИЛЬТР ОДИН В ОКНЕ, ТО В ОСОБЕННОСТИ*/

        filtersMap.forEach((key, setVals) ->
        {
            List<String> checkingVals = setVals.stream().limit(3).collect(Collectors.toList());

            if (isDiapasonVals(checkingVals))
            {
                try
                {
                    TreeSet<Double> doubleVals = new TreeSet<>();
                    setVals.stream()
                            .map(stringDiapasonVal -> stringDiapasonVal.replaceAll(",","."))
                            .mapToDouble(Double::parseDouble)
                            .forEach(doubleVals::add);

                    Double firstVal = Math.floor(doubleVals.first());
                    Double lastVal = doubleVals.last();

                    doubleVals.clear();
                    doubleVals.add(firstVal);
                    doubleVals.add(lastVal);

                    diapasons.put(key, doubleVals);
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });

        return diapasons;
    }

    private Set<String> collectFeatureFilters(Set<String> features, Map<String, TreeSet<String>> filtersMap) {
        filtersMap.forEach((key, setVals) -> {
            if (!paramVals(setVals) && filterIsNotSynonym(key)) {

                features.add(key);
            }
        });

        return features;
    }

    private boolean filterIsNotSynonym(String key) {
        List<String> synonyms = Arrays.asList("Поддержка Wi-Fi", "Серия", "Венчик для взбивания","Насадки","Автоотключение","Таймер конфорок","" +
                "Плавающая головка", "Дополнительно", "Сенсорный экран", "TV-тюнер");

        for (String synonym : synonyms) {
            if (key.equalsIgnoreCase(synonym)) {
                return false;
            }
        }

        return true;
    }

    private Map<String, TreeSet<String>> collectParamFilters(Map<String, TreeSet<String>> params, Map<String, TreeSet<String>> filtersMap) {
        filtersMap.forEach((key, setVals) -> {
            if (paramVals(setVals) && !isDiapasonVals(setVals.stream().limit(3).collect(Collectors.toList()))) {
                params.put(key, setVals);
            }
        });

        return params;
    }

    private boolean paramVals(TreeSet<String> setVals) {
        String checkVal = setVals.toString();
        List<String> notParams = Arrays.asList("есть", "да", "нет");

        for (String check : notParams) {
            if (containsIgnoreCase(checkVal, check)) {
                return false;
            }
        }

        return true;
    }

    private Set<String> collectBrandFilters(Set<String> brands, List<Product> products) {
        products.forEach(product -> brands.add(StringUtils.capitalize(product.getBrand().toLowerCase())));
        return brands;
    }

    private List<Integer> collectPriceFilters(List<Integer> prices, List<Product> products) {
        int totalProducts = products.size();

        if (totalProducts == 1) {
            int onePriceVal = Iterables.getFirst(products, null).getFinalPrice();
            prices.add(onePriceVal);
            return prices;
        }

        if (totalProducts > 1) {
            products.sort(Comparator.comparingInt(Product::getFinalPrice));
            int min = Iterables.getFirst(products, null).getFinalPrice();
            int max = Iterables.getLast(products).getFinalPrice();

            prices.add(min);
            prices.add(max);

            return prices;
        }

        return new ArrayList<>();
    }

    private boolean filterIsValid(String annoKey, String annoVal) {
        boolean noDash = !annoVal.equals("-");
        boolean noZero = !annoVal.equals("0");
        boolean noNegative = !annoVal.equalsIgnoreCase("нет");

        boolean correctKey = !annoKey.equalsIgnoreCase("PRESTIGIO PTV32SS04Z")
                && !annoKey.equalsIgnoreCase("Название")
                && !annoKey.equalsIgnoreCase("Модель")
                && !annoKey.startsWith("количество шт в")
                && !annoKey.equalsIgnoreCase("Количество камер")
                && !annoKey.equals("Бренд");

        return correctKey && noDash && noZero && noNegative;
    }

    private boolean isDiapasonVals(List<String> checkingVals) {
        for (String checkingVal : checkingVals)
        {
            Pattern pattern = Pattern.compile("^[0-9]{1,10}([,.][0-9]{1,10})?$");
            Matcher matcher = pattern.matcher(checkingVal);

            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    private String getChecklist(List<Product> products) {
        Set<String> checklist = new HashSet<>();

        products.forEach(product -> {
            checklist.add(StringUtils.capitalize(product.getBrand().toLowerCase()));

            String[] shortAnno = product.getShortAnnotation().split(";");
            checklist.addAll(Arrays.asList(shortAnno));
        });
        return checklist.toString().toLowerCase();
    }

    public String createDefaultFilterChecklist(String group) {
        List<Product> products = productRepo.findProductsByProductGroupIgnoreCase(group);
        return getChecklist(products);
    }

    public Page<Product> getProductsByGroup(String group, Pageable pageable) {
        return productRepo.findByProductGroupIgnoreCaseOrderByPicAsc(group, pageable);
    }

    public List<OrderedProduct> getRecentProducts() {
        List<OrderedProduct> recentProducts;

        int all = orderedProductRepo.findAll().size();
        if (all == 0) return new ArrayList<>();
        else if (all > 0 && all < 10) return orderedProductRepo.findAll();

        recentProducts = orderedProductRepo.findAll().stream().skip(all - 10).collect(Collectors.toList());
        Collections.reverse(recentProducts);
        return recentProducts;
    }

    public List<Product> getPopularProducts() {
        List<Product> popularsProducts = new ArrayList<>();
        popularRepo.findAll().forEach(popularID -> {
            String productID = popularID.getProductID();
            Product product = productRepo.findByProductID(productID);
            if (product != null) {
                popularsProducts.add(product);
            }
            else popularRepo.delete(popularRepo.findByProductID(productID));
        });
        Collections.reverse(popularsProducts);
        return popularsProducts;
    }

    public List<Product> addPopularProducts(String productID) {
        popularRepo.save(new Popular(productID));
        return getPopularProducts();
    }

    public List<Product> deletePopularProduct(String productID) {
        popularRepo.delete(new Popular(productID));
        return getPopularProducts();
    }

    private String createComputedFilterChecklist(List<Product> products) {
        return getChecklist(products);
    }

    public Product getProductByID(String productID) {
        return productRepo.findByProductID(productID);
    }
}

    /*private boolean filterIsFeature(String filter, String supplier) {
        String[] notContains = {"X1080","''"," ГБ", "Х720", "X480", "МА*Ч"," ГЦ"," ПИНЦЕТОВ", "220 В", " КВТ","АВТООТКЛЮЧЕНИЕ","НЕРЖ."," БАР", " °C","КЭН","ПЭН", "ТЭН", " ГР","АЛЮМИНИЙ /", "T +6 - 20C", "АРТ.","ТРС-3", " Л", " СМ", " ВТ", " ОБ/МИН", " КГ", "в формате HDTV", "КУБ.М/ЧАС", " ДУХОВКА", "КРЫШКА"};
        String[] notEquals   = {"TN", "TFT","A", "A+", "B", "N", "N/ST", "R134A", "R600A", "SN/N/ST", "SN/ST", "ST"};
        String[] colors      = {"МРАМОР", "КОРИЧНЕВЫЙ", "БОРДОВЫЙ", "ВИШНЕВЫЙ", "ЗЕЛЕНЫЙ", "ЗОЛОТОЙ", "КРАСНЫЙ", "РОЗОВЫЙ", "САЛАТОВЫЙ", "БОРДО", "МРАМОР", "СЕРЕБРИСТЫЙ", "СЕРЫЙ", "СИНИЙ", "ФИСТАШКОВЫЙ", "ЦВЕТНОЙ"};
        String[] synonyms    = {"ВЛАГОЗАЩИЩЕННЫЙ КОРПУС", "СЛОТ ДЛЯ ПАМЯТИ", "САМООЧИСТКА", "ВЕРТ. ОТПАРИВАНИЕ", "АВТООТКЛЮЧЕНИЕ", "СИСТЕМА РЕВЕРСА", "3D-НАГРЕВ"};

        *//*Основное условие*//*
        if ((filter.contains(": есть") || filter.contains(": да")) || (supplier.contains("RUSBT") && !filter.contains(":")))
        {
            *//*Спецефический отсев*//* /// В мультипоток
            for (String word : notContains) {
                if (containsIgnoreCase(filter, word)) return false;
            }
            for (String word : notEquals) {
                if (equalsIgnoreCase(filter, word)) return false;
            }
            for (String word : colors) {
                if (containsIgnoreCase(filter, word)) return false;
            }
            for (String word : synonyms) {
                if (containsIgnoreCase(filter, word)) return false;
            }
            return true;
        }
        return false;
    }*/
    /*private boolean filterIsParam(String filter) {
        String[] notParams = {"нет", "0",  "-"};
        String[] notKeys = {"количество шт в"};
        String[] duplicates = {"БРИТВЕННЫХ ГОЛОВОК"};

        *//*Основное условие*//*
        if (filter.contains(":"))
        {
            for (String word : notKeys) {
                if (filter.startsWith(word)) return false;
            }
            for (String word : notParams) {
                String checkParam = substringAfter(filter, ":").trim();
                if (checkParam.startsWith(word)) return false;
            }
            for (String word : duplicates) {
                if (filter.startsWith(word)) return false;
            }
            return true;
        }
        return false;
    }*/
    /*private boolean filterIsDuplicate(String checkDuplicate, String featureFilter) {
        String[] dontMatch = {"FULL HD (1080P)", "ULTRA HD (2160P)"};

        if (containsIgnoreCase(checkDuplicate, featureFilter) & !equalsIgnoreCase(checkDuplicate, featureFilter))
        {
            for (String word : dontMatch) {
                if (word.contains(checkDuplicate)) return false;
            }
            return true;
        }
        return false;
    }*/

