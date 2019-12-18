package expert.services;
import com.fasterxml.jackson.databind.ObjectMapper;
import expert.dto.OrderedProduct;
import expert.dto.popular.Popular;
import expert.dto.popular.PopularRepo;
import expert.repos.OrderedProductRepo;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import expert.domain.Product;
import expert.dto.FiltersList;
import expert.repos.ProductRepo;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
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

    public Page<Product> getProductsByGroup(String group, Pageable pageable) {
        return productRepo.findByProductGroupIgnoreCaseOrderByPicAsc(group, pageable);
    }

    /*Создать фильтры для группы products*/
    public FiltersList createProductsFilterLists(String group) {
        FiltersList filtersList = new FiltersList();

        /*Наполнение списка товаров нужной группы*/
        List<Product> products = productRepo.findProductsByProductGroupIgnoreCase(group);

        try
        {
            /*Сформировать фильтры-бренды группы*/
            products.forEach(product -> filtersList.brands.add(StringUtils.capitalize(product.getBrand().toLowerCase())));

            /*Сформировать фильтры-цены*/
            List<Integer> allPrices = new LinkedList<>();
            products.forEach(item -> allPrices.add(item.getFinalPrice()));
            allPrices.sort(Comparator.comparingInt(Integer::intValue));
            filtersList.prices.add(allPrices.get(0));
            filtersList.prices.add(allPrices.get(allPrices.size()-1));

            /*Сформировать обрабатываемые фильтры*/
            products.forEach(product ->
            {
                String supplier   = product.getSupplier();
                String annotation = product.getAnnotation();

                /*Разбиение аннотации экземпляра Product на фильтры*/
                String splitter  = supplier.contains("RBT") ? "; " : ", ";
                String[] filters = annotation.split(splitter);

                /*Итерация и отсев неподходящих под фильтры-особенности*/
                for (String filter : filters)
                {
                    /*Сформировать фильтры-особенности*/
                    if (filterIsFeature(filter, supplier)) {
                        /*Наполнение фильтров-особенностей*/
                        filtersList.features.add(substringBefore(filter, ":").toUpperCase());

                        /*Отсев дублей фильтров и синонимов фильтров*/ ///
                        ///filtersList.features.removeIf(featureFilter -> Arrays.stream(notParams).parallel().anyMatch(filterIsDuplicate(checkDuplicate, featureFilter)));
                        List<String> remove = new ArrayList<>();
                        filtersList.features.forEach(featureFilter -> {
                            for (String checkDuplicate : filtersList.features) {
                                if (filterIsDuplicate(checkDuplicate, featureFilter)) {
                                    remove.add(checkDuplicate);
                                }
                            }
                        });
                        filtersList.features.removeAll(remove);
                    }
                    else if (filterIsParam(filter)) {
                        String key = substringBefore(filter, ":");
                        String val = substringAfter(filter, ": ");

                        key = capitalize(key);
                        val = capitalize(val);

                        /*Digit diapasons*/
                        if (filterIsDiapasonParam(val)) {
                            try {
                                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                                Number number = format.parse(val);
                                Double parsedValue = number.doubleValue();

                                if (filtersList.diapasonsFilters.get(key) != null) {
                                    TreeSet<Double> vals = filtersList.diapasonsFilters.get(key);
                                    vals.add(parsedValue);
                                    filtersList.diapasonsFilters.put(key, vals);
                                }
                                else filtersList.diapasonsFilters.putIfAbsent(key, new TreeSet<>(Collections.singleton(parsedValue)));
                            }
                            catch (ParseException e) {
                                e.getSuppressed();
                            }
                        }

                        /*Сформировать фильтры-параметры*/
                        else {
                            if (filtersList.paramFilters.get(key) != null) {
                                TreeSet<String> vals = filtersList.paramFilters.get(key);
                                vals.add(val);
                                filtersList.paramFilters.put(key, vals);
                            }
                            else filtersList.paramFilters.putIfAbsent(key, new TreeSet<>(Collections.singleton(val)));
                        }
                    }
                }
            });

            /// distinctDiapason()
            filtersList.diapasonsFilters.forEach((key, val) -> {
                Double first = Math.floor(val.first());
                Double last = val.last();
                val.clear();
                val.add(first);
                val.add(last);
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return filtersList;
    }

    private boolean filterIsFeature(String filter, String supplier) {
        String[] notContains = {"X1080","''"," ГБ", "Х720", "X480", "МА*Ч"," ГЦ"," ПИНЦЕТОВ", "220 В", " КВТ","АВТООТКЛЮЧЕНИЕ","НЕРЖ."," БАР", " °C","КЭН","ПЭН", "ТЭН", " ГР","АЛЮМИНИЙ /", "T +6 - 20C", "АРТ.","ТРС-3", " Л", " СМ", " ВТ", " ОБ/МИН", " КГ", "в формате HDTV", "КУБ.М/ЧАС", " ДУХОВКА", "КРЫШКА"};
        String[] notEquals   = {"TN", "TFT","A", "A+", "B", "N", "N/ST", "R134A", "R600A", "SN/N/ST", "SN/ST", "ST"};
        String[] colors      = {"МРАМОР", "КОРИЧНЕВЫЙ", "БОРДОВЫЙ", "ВИШНЕВЫЙ", "ЗЕЛЕНЫЙ", "ЗОЛОТОЙ", "КРАСНЫЙ", "РОЗОВЫЙ", "САЛАТОВЫЙ", "БОРДО", "МРАМОР", "СЕРЕБРИСТЫЙ", "СЕРЫЙ", "СИНИЙ", "ФИСТАШКОВЫЙ", "ЦВЕТНОЙ"};
        String[] synonyms    = {"ВЛАГОЗАЩИЩЕННЫЙ КОРПУС", "СЛОТ ДЛЯ ПАМЯТИ", "САМООЧИСТКА", "ВЕРТ. ОТПАРИВАНИЕ", "АВТООТКЛЮЧЕНИЕ", "СИСТЕМА РЕВЕРСА", "3D-НАГРЕВ"};

        /*Основное условие*/
        if ((filter.contains(": есть") || filter.contains(": да")) || (supplier.contains("RUS-BT") && !filter.contains(":")))
        {
            /*Спецефический отсев*/ /// В мультипоток
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
    }

    private boolean filterIsParam(String filter) {
        String[] notParams = {"нет", "0",  "-"};
        String[] notKeys = {"количество шт в"};
        String[] duplicates = {"БРИТВЕННЫХ ГОЛОВОК"};

        /*Основное условие*/
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
    }

    private boolean filterIsDiapasonParam(String val) {
        Pattern pattern = Pattern.compile("^[0-9]{1,10}([,.][0-9]{1,10})?$");
        Matcher matcher = pattern.matcher(val);
        return !val.equals("0") && matcher.matches();
    }

    private boolean filterIsDuplicate(String checkDuplicate, String featureFilter) {
        String[] dontMatch = {"FULL HD (1080P)", "ULTRA HD (2160P)"};

        if (containsIgnoreCase(checkDuplicate, featureFilter) & !equalsIgnoreCase(checkDuplicate, featureFilter))
        {
            for (String word : dontMatch) {
                if (word.contains(checkDuplicate)) return false;
            }
            return true;
        }
        return false;
    }

    public Product getProductByID(String productID) {
        return productRepo.findByProductID(productID);
    }


    /*
    Основной алгоритм фильтрации
    Main sort algorithm
    */
    public Page<Product> filterProducts(Map<String, String[]> filters, String group, Pageable pageable) {
        List<Product> products = productRepo.findByProductGroupIgnoreCase(group);



        ///
        /*
        Коллекция с фильтрами Queue/LinkedList наполняется и убирается, фильтруется для каждого фильтра в ней
        Новый фильтр добавляется к уже существующему друг за другом
        При снятии фильтр убирается из очереди в обратном порядке
        *
        При каждой фильтрации формирование нового списка фильтров
        Сверка уже наполненого списка фильтров с новым, если старый не присутствует в новом, то старый :disabled
        Выводить фильтры от rbt и подставлять синонимы по ключам из json
        *
        for (map.entry filter : filters)
        */
        /*
        * Убрать пыстые массивы из объекта фильтров
        * */
        filters.forEach((s, strings) -> log.info(s + " : " + Arrays.toString(strings)));



        /// Basic filtration algorithm
        try
        {
            /*Price filters*/
            products = products.stream().filter(product ->
            {
                String[] priceFilters = filters.get("prices");
                int minPrice = Integer.parseInt(priceFilters[0].trim());
                int maxPrice = Integer.parseInt(priceFilters[1].trim());
                return product.getFinalPrice() >= minPrice && product.getFinalPrice() <= maxPrice;
            }).collect(Collectors.toList());

            /*Brands filters*/
            if (filterHasContent(filters, "brands")) {
                products = products.stream().filter(product ->
                {
                    String brandFilters = Arrays.toString(filters.get("brands"));
                    return containsIgnoreCase(brandFilters, product.getBrand().trim());
                }).collect(Collectors.toList());
            }

            /*Diapasons filters*/
            if (filterHasContent(filters, "selectedDiapasons")) {
                products = products.stream().filter(product ->
                {
                    String[] selectedDiapasons = filters.get("selectedDiapasons");
                    String annotation = product.getShortAnnotation();

                    for (String diapason : selectedDiapasons)
                    {
                        String diapasonKey  = substringBefore(diapason, ":");
                        Double minimum = Double.parseDouble(substringBetween(diapason, ":",","));
                        Double maximum = Double.parseDouble(substringAfter(diapason, ","));

                        if (minimum == null || maximum == null) return false;

                        if (annotation.contains(diapasonKey)) {
                            String parseValue = substringBetween(annotation, diapasonKey, ";");
                            if (parseValue.contains(": ")) parseValue = substringAfter(parseValue, ": ");

                            Double checkVal = Double.parseDouble(parseValue.replaceAll(",","."));
                            if (checkVal == null || checkVal < minimum || checkVal > maximum) return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            /*Фильтры по параметрам*/
            if (filterHasContent(filters, "params")) {
                products = products.stream().filter(product ->
                {
                    String annotation = product.getShortAnnotation();

                    for (String param : filters.get("params")) {
                        if (containsIgnoreCase(annotation, param)) return true;
                    }
                    return false;
                }).collect(Collectors.toList());
            }

            /*Фильтры по особенностям*/
            if (filterHasContent(filters, "features")) {
                products = products.stream().filter(product ->
                {
                    String annotation = product.getShortAnnotation();
                    for (String feature : filters.get("features")) {
                        if (containsIgnoreCase(annotation, feature)) return true;
                    }
                    return false;
                }).collect(Collectors.toList());
            }

            products.sort(Comparator.comparingInt(Product::getFinalPrice).thenComparing(Product::getPic));
        }
        catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }

        return productsPage(products, pageable);
    }

    private boolean filterHasContent(Map<String, String[]> filters, String selectedDiapasons) {
        return !Arrays.toString(filters.get(selectedDiapasons)).equals("[]");
    }

    private Page<Product> productsPage(List<Product> products, Pageable pageable) {
        try{
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), products.size());
            return new PageImpl<>(products.subList(start, end), pageable, products.size());
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
                log.info("\nreq: " + request);
                products = products.stream()
                        .filter(product -> containsIgnoreCase(product.getSearchName(), request))
                        .collect(Collectors.toList());
            }
            products = products.stream().limit(10).collect(Collectors.toList());
        }

        return products;
    }

    public void test() {

        Set<String> categoryGroups = new TreeSet<>();
        productRepo.findAll().forEach(product -> categoryGroups.add(product.getProductGroup()));

        Map<String, String> groups = new LinkedHashMap<>();

        categoryGroups.forEach(productGroup -> {
            Product product = productRepo.findFirstByProductGroupAndPicIsNotNullAndPicNotContains(productGroup, "legprom71");
            String pic = product != null ? product.getPic() : "D:\\Projects\\Rest\\src\\main\\resources\\static\\pics\\toster.png";

            groups.put(productGroup, pic);

            /*group.add(productGroup);
            group.add(pic);*/
        });

        try {
            new ObjectMapper().writeValue(new File("D:\\Projects\\Rest\\src\\main\\resources\\static\\js\\assets\\json\\groups.json"), groups);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<OrderedProduct> getRecentProducts() {
        int all = orderedProductRepo.findAll().size();
        List<OrderedProduct> recentProducts = orderedProductRepo.findAll().stream().skip(all - 10).collect(Collectors.toList());
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
}
