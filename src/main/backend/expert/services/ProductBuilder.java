package expert.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import expert.config.JsonConfig;
import expert.domain.OriginalProduct;
import expert.domain.Product;
import expert.domain.UniqueBrand;
import expert.repos.BrandsRepo;
import expert.repos.OrderRepo;
import expert.repos.OriginalRepo;
import expert.repos.ProductRepo;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
@Service
@AllArgsConstructor
public class ProductBuilder {
    private final OriginalRepo originalRepo;
    private final ProductRepo productRepo;
    private final JsonConfig jsonConfig;
    private final BrandsRepo brandsRepo;
    private final OrderRepo orderRepo;

    public int updateProductsDB(ArrayList<MultipartFile> supplierCatalogs) {

        parseToOriginalProducts(supplierCatalogs);

        matchProducts();

        resolveDuplicates();

        checkProductsCorrect();

        mapCatalogJSON();

        checkOrdersForProductAvailable();

        int totalProducts = productRepo.findAll().size();
        log.info("Update Complete! Products available: " + totalProducts);

        return totalProducts;
    }

    /*Обновление данных таблицы поставщиков*/
    private void parseToOriginalProducts(ArrayList<MultipartFile> supplierCatalogs) {
        try
        {
            for (MultipartFile excelFile : supplierCatalogs)
            {
                System.out.println();
                log.info("Парсинг: " + excelFile.getOriginalFilename());

                try (XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {

                    XSSFSheet sheet = workbook.getSheetAt(0);

                    int countCreate = 0, countUpdate = 0;
                    boolean supplierRBT = excelFile.getOriginalFilename().contains("СП2");

                    for (Row row : sheet)
                    {
                        if (lineIsCorrect(row, supplierRBT))
                        {
                            String productID = resolveProductID(supplierRBT, row);

                            /*Обновить существущий OriginalProduct*/
                            if (originalProductExists(productID))
                            {
                                updateOriginalProduct(row, productID, supplierRBT);
                                countUpdate++;
                            }
                            /*Создать OriginalProduct*/
                            else
                            {
                                createOriginalProduct(row, productID, supplierRBT);
                                countCreate++;
                            }
                        }
                    }
                    log.info("Всего строк: " + sheet.getLastRowNum());
                    log.info("Создано: " + countCreate);
                    log.info("Обновлено: " + countUpdate);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (OLE2NotOfficeXmlFileException e) {
            log.warning("Формат Excel документа должен быть XLSX!");
        }
    }

    private void createOriginalProduct(Row row, String productID, boolean supplierRBT) {
        try
        {
            OriginalProduct originalProduct = new OriginalProduct();
            originalProduct.setProductID(productID);

            String originalCategory     = supplierRBT ? row.getCell(1).toString() : row.getCell(0).toString();
            String originalGroup        = supplierRBT ? "" : row.getCell(1).toString();
            String originalType         = supplierRBT ? row.getCell(2).toString() : row.getCell(3).toString();
            String originalName         = supplierRBT ? row.getCell(3).getStringCellValue() : row.getCell(6).toString();
            String originalAnnotation   = supplierRBT ? row.getCell(5).toString() : "";
            String originalPrice        = supplierRBT ? row.getCell(7).toString().trim() : row.getCell(13).toString().trim();
            String originalAmount       = supplierRBT ? row.getCell(6).toString() : row.getCell(7).toString().concat(row.getCell(8).toString());
            String originalBrand        = row.getCell(4).toString();
            String supplier             = supplierRBT ? "RBT" : "RUSBT";

            if (supplier.equals("RBT")) {
                String picLink =  StringUtils.substringBetween(row.getCell(3).toString(), "\"", "\"");
                originalProduct.setOriginalPic(picLink);
            }
            else if (!row.getCell(15).toString().isEmpty()) {
                String linkToProductPage = row.getCell(15).getHyperlink().getAddress();
                originalProduct.setLinkToPic(linkToProductPage);
            }

            originalProduct.setOriginalCategory(originalCategory);
            originalProduct.setOriginalGroup(originalGroup);
            originalProduct.setOriginalType(originalType);
            originalProduct.setOriginalName(originalName);
            originalProduct.setOriginalAnnotation(originalAnnotation);
            originalProduct.setOriginalPrice(originalPrice);
            originalProduct.setOriginalAmount(originalAmount);
            originalProduct.setOriginalBrand(originalBrand);
            originalProduct.setSupplier(supplier);

            originalProduct.setUpdateDate(LocalDate.now());
            originalRepo.save(originalProduct);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void updateOriginalProduct(Row row, String productID, boolean supplierRBT) {
        OriginalProduct originalProduct = originalRepo.findByProductID(productID);

        String newOriginalPrice  = supplierRBT ? row.getCell(7).toString() : row.getCell(13).toString();
        String newOriginalAmount = supplierRBT ? row.getCell(6).toString() : row.getCell(7).toString().concat(row.getCell(8).toString());

        originalProduct.setOriginalPrice(newOriginalPrice);
        originalProduct.setOriginalAmount(newOriginalAmount);
        originalProduct.setUpdateDate(LocalDate.now());
        originalRepo.save(originalProduct);
    }

    /*Создать оснвоной Product для вывода и работы на клиенте*/
    private void matchProducts() {
        clearProductsForUpdate();
        log.info("Маппинг группы и параметров...");

        List<OriginalProduct> originalProducts = originalRepo.findByUpdateDate(LocalDate.now());

        originalProducts.forEach(originalProduct -> {
            try
            {
                /*Разметить главные параметры для Product из заданного json файла-разметки*/
                Product product = matchProduct(originalProduct);

                /*Разметить операционные данные Product*/
                product = resolveProductsDetails(originalProduct, product);

                productRepo.save(product);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private Product matchProduct(OriginalProduct originalProduct) throws FileNotFoundException {
        String productID = originalProduct.getProductID();
        String originalType = originalProduct.getOriginalType();

        LinkedHashMap<String, String> aliases = jsonConfig.aliasesMap();

        /*Разметить данные из json каталога*/
        for (Map.Entry<String, String> aliasEntry : aliases.entrySet())
        {
            for (String alias : aliasEntry.getKey().split(","))
            {
                /*Совпадение в каталоге, разметка*/
                if (StringUtils.startsWithIgnoreCase(originalType, alias)) {
                    return matchProductJSON(aliasEntry, productID);
                }
            }
        }

        /*Если нет совпадений в json каталоге, разметить из данных поставщика*/
        return matchFromOriginalProduct(originalProduct, productID);
    }

    /*Обратока вычисляемых значений для товара*/
    private Product matchProductJSON(Map.Entry<String, String> aliasEntry, String productID) {
        Product product = new Product(productID);
        try
        {
            String[] productDetails = aliasEntry.getValue().split(",");

            Double defaultCoefficient   = Double.valueOf(productDetails[1]); /// resolveCoefficient() если product с уникальной ценой
            String productGroup         = productDetails[0];
            String singleTypeName       = productDetails[2];
            String productCategory      = productDetails[3];

            product.setProductCategory(productCategory);
            product.setProductGroup(productGroup);
            product.setDefaultCoefficient(defaultCoefficient);
            product.setSingleTypeName(singleTypeName);
            product.setMappedByJson(true);
        }
        catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }
        return product;
    }

    private Product matchFromOriginalProduct(OriginalProduct originalProduct, String productID) {
        Product product = new Product(productID);

        Double defaultCoefficient   = 1.2;
        String defaultCategory      = resolveDefaultCategory(originalProduct.getOriginalCategory());
        String defaultProductGroup  = resolveDefaultGroup(originalProduct.getOriginalType());
        String singleTypeName       = StringUtils.substringBefore(originalProduct.getOriginalName().toLowerCase(), originalProduct.getOriginalBrand().toLowerCase());

        product.setProductCategory(defaultCategory);
        product.setProductGroup(defaultProductGroup);
        product.setSingleTypeName(singleTypeName);
        product.setDefaultCoefficient(defaultCoefficient);
        return product;
    }

    private Product resolveProductsDetails(OriginalProduct originalProduct, Product product) {
        boolean supplierRBT   = originalProduct.getSupplier().equals("RBT");

        String singleTypeName = product.getSingleTypeName();
        String originalBrand  = originalProduct.getOriginalBrand();
        String originalName   = originalProduct.getOriginalName();

        String modelName      = resolveModelName(originalProduct).toUpperCase().trim();
        String annotation     = resolveAnnotation(originalProduct, supplierRBT);
        String fullName       = resolveFullName(originalName, modelName, singleTypeName, originalBrand).trim();
        String groupBrandName = resolveBrandName(singleTypeName, originalBrand).trim();
        String searchName     = resolveSearchName(originalProduct, modelName, singleTypeName).trim();
        String shortModelName = resolveShortModel(modelName).trim();
        String pic            = resolveProductPic(originalProduct);
        String shortAnnotation = resolveShortAnnotation(annotation, originalProduct);

        Integer defaultPrice  = makeRoundFinalPrice(originalProduct.getOriginalPrice(), product.getDefaultCoefficient());//resolveDefaultPrice(originalProduct, product.getDefaultCoefficient());
        Integer finalPrice    = resolveFinalPrice(originalProduct, product.getDefaultCoefficient());
        Integer bonus         = resolveBonus(finalPrice);

        if (originalProduct.getPriceModified() != null) {
            product.setPriceModified(true);
        }

        if (originalProduct.getPics() != null) {
            product.setPics(originalProduct.getPics());
        }

        if (originalProduct.getAnnotationParsed() != null) {
            product.setAnnotationParsed(true);
        }

        product.setOriginalName(originalName);
        product.setFinalPrice(finalPrice);
        product.setBonus(bonus);
        product.setModelName(modelName);
        product.setFullName(fullName);
        product.setGroupBrandName(groupBrandName);
        product.setSearchName(searchName);
        product.setShortModelName(shortModelName);
        product.setAnnotation(annotation);
        product.setShortAnnotation(shortAnnotation);

        product.setSupplier(originalProduct.getSupplier());
        product.setPic(pic);
        product.setBrand(originalBrand);
        product.setUpdateDate(LocalDate.now());

        originalProduct.setDefaultPrice(defaultPrice);
        originalProduct.setFinalPrice(finalPrice);
        originalProduct.setBonus(bonus);
        originalRepo.save(originalProduct);

        return product;
    }

    /*Обработка товаров-дубликатов*/
    private void resolveDuplicates() {
        log.info("Обработка дубликатов...");

        int count = 0;
        int before = productRepo.findAll().size();

        List<Product> products = productRepo.findBySupplier("RBT");

        for (Product product : products) {

            String shortModel = product.getShortModelName();
            String brand =  product.getBrand();

            if (!shortModel.isEmpty()) {

                List<Product> duplicates = productRepo.findBySupplierAndBrandAndShortModelNameIgnoreCase("RUSBT", brand, shortModel);

                if (!duplicates.isEmpty())
                {
                    count++;
                    duplicates.sort(Comparator.comparing(Product::getFinalPrice));

                    if (product.getFinalPrice() >= duplicates.get(0).getFinalPrice()) {
                        productRepo.deleteAll(duplicates);
                    }
                    else
                    {
                        productRepo.delete(product);
                        duplicates.stream().skip(1).forEach(productRepo::delete);
                    }
                }
            }
        }
        log.info("Товаров с дубликатами: " + count);
        log.info("BEFORE: " + before );
        log.info("AFTER: " + productRepo.findAll().size());
    }

    /*Пост-обработка разметки*/
    private void checkProductsCorrect() {
        log.info("checkProductGroupCorrect...");


        /*В ГРУППУ Кабели тв заменить в аннотации Разьем на Разъем*/

        Thread thread = new Thread(() -> productRepo.findByProductGroupIgnoreCase("Клавиатуры, мыши, комплекты").forEach(product -> {
            if (product.getSingleTypeName().contains("мышь")) {
                product.setProductGroup("Мыши");
            }
            else if (product.getSingleTypeName().contains("клавиатура")) {
                product.setProductGroup("Клавиатуры");
            }
            product.setProductCategory("Компьютеры и оргтехника");
            productRepo.save(product);
        }));
        thread.start();

        Thread thread1 = new Thread(() -> productRepo.findAll().forEach(product -> {
            if (product.getPic() == null) {
                product.setPic("https://legprom71.ru/Content/images/no-photo.png");
                productRepo.save(product);
            }
        }));
        thread1.start();

        Thread trimOriginalType = new Thread(() -> productRepo.findAllByProductCategory("Сопутствующие товары").forEach(product -> {
            String productGroup = StringUtils.substringAfter(product.getProductGroup(), " ");
            product.setProductGroup(productGroup);
            productRepo.save(product);
        }));
        trimOriginalType.start();
        /*<!--тип из аннотации в productType, если нет, то singleType-->*/
    }

    /*Проверка товаров в заказах на наличие после обновления базы*/
    private void checkOrdersForProductAvailable() {
        log.info("checkOrdersForProductAvailable...");

        orderRepo.findAllByAcceptedFalse().forEach(order -> {
            order.getOrderedProducts().entrySet().removeIf(orderedEntry -> productRepo.findByProductID(orderedEntry.getKey()) == null);

            AtomicInteger totalPrice = new AtomicInteger(), bonus = new AtomicInteger();
            order.getOrderedProducts().forEach((productID, amount) ->
            {
                Product product = productRepo.findByProductID(productID);
                totalPrice.addAndGet(product.getFinalPrice() * amount);
                bonus.addAndGet(product.getBonus() * amount);
            });

            order.setTotalPrice(totalPrice.get());
            order.setTotalBonus(bonus.get());
            orderRepo.save(order);
        });
    }

    /*Обновить каталог в JSON и сохранить в папку*/
    public void mapCatalogJSON() {
        log.info("mapCatalogJSON...");

        LinkedHashMap<String, List<ArrayList<String>>> fullCatalog = new LinkedHashMap<>();
        String[] categories = {
                "Теле-видео-аудио",
                "Кухонная техника",
                "Техника для дома",
                "Встраиваемая техника",
                "Климатическая техника",
                "Приборы персонального ухода",
                "Цифровые устройства",
                "Компьютеры и оргтехника",
                "Автотовары",
                "Строительные инструменты",
                "Подсобное хозяйство",
                "Товары для дома",
                "Отопительное оборудование",
                "Спорт и отдых",
                "Посуда и кухонные принадлежности",
                "Сопутствующие товары"
        };

        for (String category : categories)
        {
            List<ArrayList<String>> productGroups = new ArrayList<>();
            Set<String> categoryGroups = new TreeSet<>();

            productRepo.findByProductCategoryIgnoreCase(category).forEach(product -> categoryGroups.add(product.getProductGroup()));

            categoryGroups.forEach(productGroup ->
            {
                ArrayList<String> group = new ArrayList<>();
                try
                {
                    String pic;
                    Map<String, String> constantPics = jsonConfig.groupsMap();

                    pic = constantPics.get(productGroup);
                    if (pic == null) {
                        Product product = productRepo.findFirstByProductGroupAndPicIsNotNullAndPicNotContains(productGroup, "noimg_quadro");
                        pic = product != null ? product.getPic() : "https://www.euromag.ru/storage/c/2018/08/28/1535465785_727980_36.jpg";
                    }
                    group.add(productGroup);
                    group.add(pic);

                    productGroups.add(group);
                }
                catch (NullPointerException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            });

            fullCatalog.put(category, productGroups);
        }
        try
        {
            new ObjectMapper().writeValue(new File("D:\\Projects\\ExpertRestRELEASE\\src\\main\\resources\\static\\js\\assets\\json\\catalog.json"), fullCatalog);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> parseRUSBTParams() {
        AtomicInteger count404 = new AtomicInteger();
        AtomicInteger countPic = new AtomicInteger();
        AtomicInteger countAnno = new AtomicInteger();
        AtomicInteger connectionError = new AtomicInteger();

        List<OriginalProduct> originalProducts = originalRepo.findByOriginalPicIsNullAndSupplierAndLinkToPicIsNotNull("RUSBT");

        System.out.println();
        log.info("Парсинг сайта картинок RUSBT");
        log.info("Всего товаров без картинок для парсинга: " + originalProducts.size());

        originalProducts.forEach(originalProduct ->
        {
            Product product = productRepo.findByProductID(originalProduct.getProductID());

            String link = originalProduct.getLinkToPic();
            log.info(link);

            try
            {
                Document page = Jsoup.connect(link).get();

                System.out.println();
                log.info("Парсинг: " + originalProduct.getOriginalName());

                Thread parsePics = new Thread(() ->
                {
                    Elements pics = page.getElementsByClass("cnt_item");

                    if (pics.isEmpty()) {
                        log.info("Нет изображения товара на сайте!");
                    }
                    else
                    {
                        int picsFound = pics.size();
                        log.info("Изображений товара: " + picsFound);

                        String firstPic = "http://rusbt.ru".concat(StringUtils.substringBetween(pics.get(0).toString(), "url('", "');"));
                        originalProduct.setOriginalPic(firstPic);
                        product.setPic(firstPic);
                        log.info(firstPic);

                        if (picsFound > 1)
                        {
                            String fewPics = originalProduct.getPics() != null ? originalProduct.getPics() : "";

                            for (Element pic : pics) {
                                String anotherPic = "http://rusbt.ru".concat(StringUtils.substringBetween(pic.toString(), "url('", "');"));
                                fewPics = fewPics.concat(anotherPic).concat(";");
                            }

                            log.info("fewPics: " + fewPics);

                            originalProduct.setPics(fewPics);
                            product.setPics(fewPics);
                        }
                        originalRepo.save(originalProduct);
                        productRepo.save(product);
                        countPic.getAndIncrement();
                    }
                });

                Thread parsInfo = new Thread(() ->
                {
                    /*исключить Деколь и установить modelName*/
                    Element productParams = page.getElementById("tabs-2");
                    Elements productInfo = page.getElementsByClass("bx_item_description");

                    Elements infoKeys = productParams.getElementsByClass("left_prop");
                    Elements infoVals = productParams.getElementsByClass("left_value");

                    String annotation = "";

                    if (!productInfo.isEmpty()) {
                        String aboutProduct = productInfo.text();
                        originalProduct.setInfoFromRusbt(aboutProduct);
                    }

                    if (!infoKeys.isEmpty())
                    {
                        for (int i = 0; i < infoKeys.size(); i++) {
                            String key = infoKeys.get(i).getElementsByTag("span").text();
                            String val = infoVals.get(i).ownText();

                            String anno = key.concat(": ").concat(val).concat(";");
                            annotation = annotation.concat(anno);

                            if (key.startsWith("Модель")) {
                                originalProduct.setParsedModelName(val);
                                originalProduct.setModelNameParsed(true);
                            }

                            log.info(key + ": " + val);
                        }
                    }

                    originalProduct.setOriginalAnnotation(annotation);
                    originalProduct.setAnnotationParsed(true);

                    product.setAnnotation(annotation);
                    product.setAnnotationParsed(true);
                    product.setShortAnnotation(resolveShortAnnotation(annotation, originalProduct));

                    originalRepo.save(originalProduct);
                    productRepo.save(product);

                    countAnno.getAndIncrement();
                    log.info("ANNOTATION: " + annotation);
                    log.info("INFO: " + productInfo.text());
                });

                parsePics.start();
                parsInfo.start();
            }
            catch (HttpStatusException exp) {
                log.info("404 Page is empty");
                count404.getAndIncrement();
            }
            catch (ConnectException exp) {
                connectionError.getAndIncrement();
                log.info("Connection timed out");
                exp.printStackTrace();
            }
            catch (MalformedURLException exp) {
                log.info(exp.getClass().getName());
            }
            catch (IOException | NullPointerException exp) {
                exp.printStackTrace();
            }
        });

        Map<String, Integer> parsingResult = new HashMap<>();
        parsingResult.put("Всего товаров: ", originalProducts.size());
        parsingResult.put("Товаров с изображениями: ", countPic.intValue());
        parsingResult.put("Товаров с аннотацией: ", countAnno.intValue());
        parsingResult.put("404 на сайте: ", count404.intValue());
        parsingResult.put("ConnectException: ", connectionError.intValue());
        parsingResult.forEach((s, integer) -> log.info(s + ": " + integer));


        productRepo.findAll().forEach(product -> {
            product.setShortAnnotation(resolveShortAnnotation(product.getAnnotation(), originalRepo.findByProductID(product.getProductID())));
        });

        mapCatalogJSON();
        log.info("Done!");

        return parsingResult;
    }

    /*Загрузить прайсы со специальными товарами от поставщика*/
    public void updateBrandsPrice(ArrayList<MultipartFile> brandsCatalogs) {
        log.info(brandsCatalogs.size() + "");
        int count = 0;

        for (MultipartFile file : brandsCatalogs) {
            log.info(file.getOriginalFilename());

            try(CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(file.getInputStream())), ';'))
            {
                for (String[] line: reader)
                {
                    try
                    {
                        if (!line[0].isEmpty()) {
                            System.out.println();
                            log.info(Arrays.toString(line));
                            log.info(line[0]);

                            UniqueBrand brandProduct = brandsRepo.findByProductID(line[0]);

                            if (brandProduct == null) {
                                brandProduct = new UniqueBrand();
                                brandProduct.setProductID(line[0]);
                            }

                            brandProduct.setFullName(line[1]);
                            brandProduct.setBrand(line[2]);
                            brandProduct.setAnnotation(line[3].trim());
                            brandProduct.setOriginalPrice(line[4].trim());
                            brandProduct.setFinalPrice(line[5].trim());
                            brandProduct.setPercent(line[6]);

                            brandsRepo.save(brandProduct);
                            log.info(brandProduct.getFullName());
                            count++;
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException exp) {
                        exp.printStackTrace();
                    }
                }
            }
            catch (IOException exp) {
                exp.printStackTrace();
            }
        }
        log.info("Обработано: " + count);
    }

    private String resolveProductPic(OriginalProduct originalProduct) {
        return originalProduct.getOriginalPic() != null ? originalProduct.getOriginalPic() : "http://rusbt.ru/bitrix/templates/main_s2/images/noimg/noimg_quadro.jpg"; ///  https://legprom71.ru/Content/images/no-photo.png
    }

    private String resolveShortAnnotation(String annotation, OriginalProduct originalProduct) {
        if (!annotation.isEmpty())
        {
            String shortAnnotation;
            String[] stopList = {": нет", ": Нет", ": 0",  ": -", "количество шт в", "Количество изделий"};

            List<String> filters = new LinkedList<>(Arrays.asList(annotation.split(";")));
            filters.removeIf(filter -> Arrays.stream(stopList).parallel().anyMatch(filter::contains));

            shortAnnotation = filters.toString().replaceAll("\\[|\\]","");

            if (!shortAnnotation.endsWith(";")) {
                shortAnnotation = shortAnnotation.concat(";");
            }
            shortAnnotation = shortAnnotation.replaceAll(", ", ";");

            return shortAnnotation;
        }
        return "Original Annotation is empty!";
    }

    private boolean originalProductExists(String productID) {
        return originalRepo.findByProductID(productID) != null;
    }

    private String resolveProductID(boolean supplierRBT, Row row) {
        return supplierRBT ? row.getCell(0).toString() : row.getCell(5).toString().replaceAll("\\\\", "_");
    }

    private boolean lineIsCorrect(Row row, boolean supplierRBT) throws NullPointerException {
        String firstCell;
        if (row.getCell(0) != null) {
            firstCell = row.getCell(0).getStringCellValue();
        }
        else return false;

        if (supplierRBT) {
            return (!firstCell.isEmpty() && !firstCell.startsWith("8(351)") && !firstCell.startsWith(".") && !firstCell.startsWith("г. Челябинск") && !firstCell.startsWith("Код товара"));
        }
        return (!firstCell.isEmpty() && !StringUtils.containsIgnoreCase(firstCell, "Уценка") && !firstCell.contains("Группа 1"));
    }

    private String resolveDefaultGroup(String originalType) {
        if (originalType.contains("_")) {
            originalType = StringUtils.substringAfter(originalType, "_");
        }
        return originalType;
    }

    private String resolveDefaultCategory(String originalCategory) {
        if (originalCategory.contains("_")) {
            originalCategory = StringUtils.substringAfter(originalCategory, "_");
        }
        switch (originalCategory) {
            case "Аудио"                    : return "Автотовары";
            case "Электроника"              : return "Сопутствующие товары";
            case "Крупная техника для кухни":
            case "Мелкая техника для кухни" : return "Кухонная техника";
            case "Красота и здоровье"       : return "Приборы персонального ухода";
            case "Инструменты для дома, дачи и авто":
            case "Инструмент"               : return "Строительные инструменты";
            case "Гаджеты"                  : return "Цифровые устройства";
            case "Отдых и Развлечения"      : return "Спорт и отдых";
            default: return originalCategory;
        }
    }

    private String resolveShortModel(String modelName) {
        Pattern pattern = Pattern.compile("^[\\w\\W\\s?]+ [А-ЯA-Z\\W?]{3,20}$");
        Matcher matcher = pattern.matcher(modelName);

        String shortModelName;

        if (matcher.matches()) {
            shortModelName = StringUtils.substringBeforeLast(modelName, " ").replaceAll(" ", "");
        }
        else shortModelName = modelName.replaceAll(" ", "");

        //return shortModelName.replaceAll("-", "").replaceAll("_", "").replaceAll("\\.", "").replaceAll(",", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("/", "").toLowerCase();

        return shortModelName.replaceAll("\\s|\\p{P}","");
    }

    private String resolveAnnotation(OriginalProduct originalProduct, boolean supplierRBT) {
        String annotation;
        if (supplierRBT || originalProduct.getAnnotationParsed() != null) {
            annotation = originalProduct.getOriginalAnnotation();
            return !annotation.isEmpty() ? annotation : originalProduct.getOriginalName();
        }
        annotation = StringUtils.substringAfter(originalProduct.getOriginalName(), ", ");
        return !annotation.isEmpty() ? annotation : originalProduct.getOriginalName();
    }

    private String resolveSearchName(OriginalProduct originalProduct, String modelName, String singleType) {
        String searchName;

        if (!modelName.isEmpty()) {
            searchName = singleType.concat(originalProduct.getOriginalBrand().concat(modelName));
        }
        else {
            searchName = originalProduct.getOriginalName();
        }

        return searchName.toLowerCase().replaceAll(" ","").concat(searchName.toLowerCase().replaceAll("\\s|\\p{P}",""));
    }

    private String resolveBrandName(String singleTypeName, String originalBrand) {
        return singleTypeName.concat(" ").concat(StringUtils.capitalize(originalBrand.toLowerCase()));
    }

    private Integer resolveBonus(Integer finalPrice) {
        int bonus = finalPrice * 3 / 100;
        String bonusToRound = String.valueOf(bonus);

        if (bonus > 0 && bonus <= 10) {
            return 10;
        }
        else
        {
            bonusToRound = bonusToRound.substring(0, bonusToRound.length()-1).concat("0");
            return Integer.parseInt(bonusToRound);
        }
    }

    private Integer resolveFinalPrice(OriginalProduct originalProduct, Double coefficient) {
        int finalPrice;

        if (originalProduct.getPriceModified() != null) {
            finalPrice = originalProduct.getFinalPrice();
        }
        else if (productWithUniquePrice(originalProduct)) {
            finalPrice = Integer.parseInt(StringUtils.deleteWhitespace(brandsRepo.findByProductID(originalProduct.getProductID()).getFinalPrice()));
        }
        else {
            finalPrice = makeRoundFinalPrice(originalProduct.getOriginalPrice(), coefficient);
        }

        return finalPrice;
    }

    private boolean productWithUniquePrice(OriginalProduct originalProduct) {
        String originalBrand = originalProduct.getOriginalBrand().toUpperCase();
        String[] uniqueBrands = {"ARDIN","SENTORE","BINATONE","AMCV","DOFFLER","DOFFLER PLUS","EXCOMP","LERAN"};
        if (Arrays.asList(uniqueBrands).contains(originalBrand)) {
            return brandsRepo.findByProductID(originalProduct.getProductID()) != null;
        }
        return false;
    }

    private Integer makeRoundFinalPrice(String originalPrice, Double coefficient) {
        int finalPrice = (int) (Double.parseDouble(originalPrice) * coefficient);
        String finalPriceToRound = String.valueOf(finalPrice);

        if (finalPrice > 0 && finalPrice <= 10) {
            return 10;
        }
        else if (finalPrice > 10 && finalPrice < 1000) {
            finalPriceToRound = finalPriceToRound.substring(0, finalPriceToRound.length()-1).concat("9");
            return Integer.parseInt(finalPriceToRound);
        }
        else if (finalPrice > 1000) {
            finalPriceToRound = finalPriceToRound.substring(0, finalPriceToRound.length()-2).concat("90");
            return Integer.parseInt(finalPriceToRound);
        }
        else return finalPrice;
    }

    private String resolveFullName(String originalName, String modelName, String singleTypeName, String originalBrand) {
        String fullName;

        if (modelName.isEmpty()) {
            return StringUtils.capitalize(singleTypeName.concat(" ").concat(originalName));
        }

        fullName = singleTypeName.trim().concat(" ").concat(StringUtils.capitalize(originalBrand.toLowerCase()).trim()).concat(" ").concat(modelName.trim());

        if (fullName.isEmpty()) {
            fullName = StringUtils.capitalize(StringUtils.substringBefore(originalName, ", "));
        }

        return StringUtils.capitalize(fullName);
    }

    private String resolveModelName(OriginalProduct originalProduct) {
        String originalBrand = originalProduct.getOriginalBrand().toLowerCase();
        String originalName = originalProduct.getOriginalName().toLowerCase();
        String modelName;

        /*Если modelName взято из базы поставщика*/
        if (originalProduct.getModelNameParsed() != null) {
            return originalProduct.getParsedModelName();
        }

        /*Формирование modelName*/
        if (originalProduct.getSupplier().equals("RBT") && !originalBrand.isEmpty()) {
            modelName = StringUtils.substringAfter(originalProduct.getOriginalName().toLowerCase(), originalBrand.toUpperCase().replaceAll(" ", "").replaceAll("&", "")).trim();
        }
        else if (originalName.contains(", ") && originalName.contains(originalBrand) && StringUtils.substringAfter(originalName, originalBrand).contains(",")) {
            modelName = StringUtils.substringBetween(originalName, originalBrand, ",").trim();
        }
        else modelName = StringUtils.substringAfter(originalName, originalBrand).trim();

        /*Проверка и отправка modelName*/
        if (!modelName.isEmpty()) {
            return modelName;
        }
        else if (originalName.contains(originalBrand)) {
            return StringUtils.substringAfter(originalName, originalBrand);
        }
        else return "";
    }

    private void clearProductsForUpdate() {
        log.info("Очищение Products...");
        productRepo.deleteAll();
    }

    public boolean downloadImage(Map<String, String> data) {

        String productID = data.get("productID");
        String link = data.get("link");
        String fullPath = "D:\\Projects\\ExpertRestRELEASE\\src\\main\\resources\\static\\img\\products\\" + productID + ".png";
        String relPath = "../../img/products/" + productID + ".png";

        log.info(link);
        log.info(fullPath);

        try (FileOutputStream out = (new FileOutputStream(new java.io.File(fullPath))))
        {
            Connection.Response resultImageResponse = Jsoup.connect(link).ignoreContentType(true).execute();
            out.write(resultImageResponse.bodyAsBytes());

            OriginalProduct originalProduct = originalRepo.findByProductID(productID);
            originalProduct.setOriginalPic(relPath);
            originalRepo.save(originalProduct);

            Product product = productRepo.findByProductID(productID);
            product.setPic(relPath);
            productRepo.save(product);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        log.info(productRepo.findByProductID(productID).getPic());
        return new File(fullPath).exists();
    }

    public Product setCustomPrice(Map<String, String> priceUpdate) {
        String productID = priceUpdate.get("productID");
        Integer newPrice = Integer.parseInt(priceUpdate.get("newPrice"));

        OriginalProduct originalProduct = originalRepo.findByProductID(productID);
        originalProduct.setFinalPrice(newPrice);
        originalProduct.setPriceModified(true);
        originalRepo.save(originalProduct);

        Product product = productRepo.findByProductID(productID);
        product.setFinalPrice(newPrice);
        product.setPriceModified(true);
        productRepo.save(product);

        return product;
    }

    public Product restoreDefaultPrice(String productID) {
        log.info(productID);

        OriginalProduct originalProduct = originalRepo.findByProductID(productID);
        originalProduct.setPriceModified(null);
        originalProduct.setFinalPrice(originalProduct.getDefaultPrice());
        originalRepo.save(originalProduct);

        Product product = productRepo.findByProductID(productID);
        product.setFinalPrice(originalProduct.getDefaultPrice());
        product.setPriceModified(null);
        productRepo.save(product);

        return product;
    }

    public void resetDB() {
        log.info("Resetting DB...");
        originalRepo.findAll().forEach(originalProduct -> {
            originalProduct.setUpdateDate(LocalDate.ofYearDay(2000, 1));
            originalRepo.save(originalProduct);
        });
        log.info("Original DB reset");
    }

    public void test() {
        log.info("test");
    }
}
