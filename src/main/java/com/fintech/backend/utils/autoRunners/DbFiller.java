package com.fintech.backend.utils.autoRunners;

import com.fintech.backend.models.*;
import com.fintech.backend.repository.*;
import com.fintech.backend.service.Scheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbFiller implements ApplicationRunner {

    private final Scheduler scheduler;
    private final CategoryRepository categoryRepository;
    private final ProductsRepository productsRepository;
    private final StoresRepository storesRepository;
    private final ChainRepository chainRepository;
    private final ProductPricesRepository productPricesRepository;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        scheduler.createMonthlySavingGoals();
        loadCategories();
        loadStoreCsvFileToDatabase();
        loadProductCsvFileToDatabase();
    }

    private void loadCategories() {
        if (categoryRepository.findAll().isEmpty()) {
            List<Category> categoryList = new ArrayList<>();
            categoryList.add(new Category("CAT-XXX1", "FOOD"));
            categoryList.add(new Category("CAT-XXX2", "TRANSPORT"));
            categoryList.add(new Category("CAT-XXX3", "MEDICINE"));
            categoryList.add(new Category("CAT-XXX4", "GROCERIES"));
            categoryList.add(new Category("CAT-XXX5", "RENT"));
            categoryList.add(new Category("CAT-XXX6", "INSURANCE"));
            categoryList.add(new Category("CAT-XXX7", "SUBSCRIPTIONS"));
            categoryList.add(new Category("CAT-XXX8", "ENTERTAINMENT"));
            categoryList.add(new Category("CAT-XXX0", "OTHER"));
            categoryRepository.saveAll(categoryList);
            log.info("Categories loaded successfully");
        } else {
            log.info("Categories already exist");
        }
    }

    public void loadStoreCsvFileToDatabase() {
        final String CSV_CLASSPATH = "dummyData/pune_supermarket_database.csv";
        List<Stores> stores = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource(CSV_CLASSPATH);
            if (!resource.exists()) {
                log.warn("CSV resource not found on classpath: {}", CSV_CLASSPATH);
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                // Skip header
                String header = br.readLine();
                if (header == null) {
                    log.warn("CSV file is empty: {}", CSV_CLASSPATH);
                    return;
                }

                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] values = parseCsvLine(line);
                    // Expected 5 fields: chain, storeName, locality, fullAddress, pincode
                    if (values.length != 5) {
                        log.warn("Skipping line due to incorrect number of fields (expected 5, found {}): {}", values.length, line);
                        continue;
                    }
                    try {

                        String chainName = values[0].trim();
                        String storeTypeOrName = values[1].trim();
                        String locality = values[2].trim();
                        String fullAddress = values[3].trim();
                        Long pincode = Long.valueOf(values[4].trim());

                        Chian existing = chainRepository.findByChainName(chainName);
                        Chian chain = (existing == null ? chainRepository.save(new Chian(chainName)) : existing);

                        Stores store = new Stores(
                                chain,
                                storeTypeOrName,
                                locality,
                                fullAddress,
                                pincode
                        );
                        stores.add(store);
                    } catch (NumberFormatException e) {
                        log.warn("Skipping line due to invalid pincode: {}", line);
                    } catch (Exception e) {
                        log.warn("Skipping line due to mapping error: {} | error: {}", line, e.getMessage());
                    }
                }
            }

            if (!stores.isEmpty()) {
                storesRepository.saveAll(stores);
                log.info("Successfully loaded {} store locations from {}", stores.size(), CSV_CLASSPATH);
            } else {
                log.info("No store records parsed from {}", CSV_CLASSPATH);
            }

        } catch (Exception e) {
            log.error("Error processing CSV file '{}': {}", CSV_CLASSPATH, e.getMessage(), e);
        }
    }

    public void loadProductCsvFileToDatabase() {
        final String CSV_CLASSPATH = "dummyData/product_data.csv";
        List<Products> products = new ArrayList<>();
        List<ProductPrices> productPrices = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource(CSV_CLASSPATH);
            if (!resource.exists()) {
                log.warn("CSV resource not found on classpath: {}", CSV_CLASSPATH);
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                // Skip header
                String header = br.readLine();
                if (header == null) {
                    log.warn("CSV file is empty: {}", CSV_CLASSPATH);
                    return;
                }

                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] values = parseCsvLine(line);
                    // Expected 5 fields: chain, storeName, locality, fullAddress, pincode
                    if (values.length != 8) {
                        log.warn("Skipping line due to incorrect number of fields (expected 5, found {}): {}", values.length, line);
                        continue;
                    }
                    try {

                        String productCategory = values[0].trim();
                        String productName = values[1].trim();
                        String brand = values[2].trim();
                        String standardUnit = values[3].trim();
                        String offerType = values[4].trim();
                        Double dMartPrice = Double.valueOf(values[5].trim());
                        Double starPrice = Double.valueOf(values[6].trim());
                        Double reliancePrice = Double.valueOf(values[7].trim());

                        Products product = new Products(productCategory, productName, brand, standardUnit, offerType);
                        products.add(product);

                        productPrices.add(new ProductPrices(product,chainRepository.findByChainName("D-Mart"),dMartPrice, LocalDate.now()));
                        productPrices.add(new ProductPrices(product,chainRepository.findByChainName("Star Bazaar"),starPrice, LocalDate.now()));
                        productPrices.add(new ProductPrices(product,chainRepository.findByChainName("Reliance Mart"),reliancePrice, LocalDate.now()));
                    } catch (NumberFormatException e) {
                        log.warn("Skipping line due to details: {}", line);
                    } catch (Exception e) {
                        log.warn("Skipping line due to mapping error: {} | error: {}", line, e.getMessage());
                    }
                }
            }

            if (!products.isEmpty()) {
                productsRepository.saveAll(products);
                productPricesRepository.saveAll(productPrices);
                log.info("Successfully loaded {} store locations from {}", products.size(), CSV_CLASSPATH);
            } else {
                log.info("No store records parsed from {}", CSV_CLASSPATH);
            }

        } catch (Exception e) {
            log.error("Error processing CSV file '{}': {}", CSV_CLASSPATH, e.getMessage(), e);
        }
    }

    // Minimal CSV parser: supports quoted fields with commas and double-quote escaping per RFC 4180
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // Escaped quote
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}