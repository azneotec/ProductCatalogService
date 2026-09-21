package com.azneotech.productcatalogservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "catalog.seed.enabled=false")
class ProductCatalogServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
