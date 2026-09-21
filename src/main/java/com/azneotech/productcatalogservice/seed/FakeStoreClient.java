package com.azneotech.productcatalogservice.seed;

import com.azneotech.productcatalogservice.dtos.FakeStoreProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/** Read-only client for the public FakeStore API, used only to seed the catalog. */
@Component
public class FakeStoreClient {

    private final RestTemplate restTemplate;

    public FakeStoreClient(RestTemplateBuilder restTemplateBuilder,
                           @Value("${catalog.seed.fakestore.base-url:https://fakestoreapi.com}") String baseUrl) {
        // Timeouts matter: this runs during boot and an unreachable host must not stall startup.
        this.restTemplate = restTemplateBuilder
                .rootUri(baseUrl)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /** @throws RestClientException on any transport or HTTP error. */
    public List<FakeStoreProductDto> fetchAllProducts() throws RestClientException {
        FakeStoreProductDto[] products = restTemplate.getForObject("/products", FakeStoreProductDto[].class);
        return products == null ? List.of() : Arrays.asList(products);
    }
}
