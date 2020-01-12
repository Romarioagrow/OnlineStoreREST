package expert.services;

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

    /*Основной алгоритм фильтрации*/
    public LinkedList<Object> filterProducts(LinkedList<String> filters, String group, Pageable pageable) {
        List<Product> products = productRepo.findByProductGroupIgnoreCase(group);

        for (Object filter : filters) {
            products = filteringProducts(products, filter.toString());
        }

        String filterCheckList = createComputedFilterChecklist(products);

        return productsPage(products, pageable, filterCheckList);
    }

    private List<Product> filteringProducts(List<Product> products, String filter) {
        String filterKey  = substringBefore(filter, ";");
        String filterName = substringAfter(filter, ";");

        switch (filterKey) {
            case "price": return filterProductsByPrice(products, filterName);
            case "param": return filterProductsByParams(products, filterName);
            case "brand": return filterProductsByBrands(products, filterName);
            case "feature": return filterProductsByFeature(products, filterName);
            case "diapason": return filterProductsByDiapasons(products, filterName);
            default: return products;
        }
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

    /*Создать фильтры для группы products*/
    public FiltersList createProductsFilterLists(String group) {
        FiltersList filtersList = new FiltersList();

        /*Наполнение списка товаров нужной группы*/
        List<Product> products = productRepo.findProductsByProductGroupIgnoreCase(group);

        try
        {
            filtersList.checklist = createDefaultFilterChecklist(group);

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
                if (product.getSupplier().equals("RBT")) {

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

    /// ОБЪЕДЕНИТЬ
    public String/*Set<String>*/ createDefaultFilterChecklist(String group) {
        List<Product> products = productRepo.findProductsByProductGroupIgnoreCase(group);
        Set<String> checklist = new HashSet<>();

        products.forEach(product -> {
            checklist.add(StringUtils.capitalize(product.getBrand().toLowerCase()));

            String[] shortAnno = product.getShortAnnotation().split(";");
            checklist.addAll(Arrays.asList(shortAnno));
        });
        return checklist.toString().toLowerCase();
    }
    private String createComputedFilterChecklist(List<Product> products) {
        Set<String> checklist = new HashSet<>();

        products.forEach(product -> {
            checklist.add(StringUtils.capitalize(product.getBrand().toLowerCase()));

            String[] shortAnno = product.getShortAnnotation().split(";");
            checklist.addAll(Arrays.asList(shortAnno));
        });
        return checklist.toString().toLowerCase();
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
}
