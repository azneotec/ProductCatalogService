package com.azneotech.productcatalogservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "catalog.seed.enabled=false")
@ActiveProfiles("test")
class ProductCatalogServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
