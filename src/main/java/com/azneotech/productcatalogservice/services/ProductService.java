package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.FakeStoreProductDto;
import com.azneotech.productcatalogservice.exceptions.FakeStoreApiExceptionType;
import com.azneotech.productcatalogservice.exceptions.FakeStoreApiProductException;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductService implements IProductService {

    private static final String FAKE_STORE_API_BASE_URL = "https://fakestoreapi.com";

    private RestTemplate restTemplate;
    private final RestTemplateBuilder restTemplateBuilder;

    public ProductService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = restTemplateBuilder
                    .rootUri(FAKE_STORE_API_BASE_URL)
                    .build();
        }
        return restTemplate;
    }

    @Override
    public Product getProductDetailsById(Long id) {
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<FakeStoreProductDto> responseEntity = restTemplate.getForEntity(
                "/products/{id}",
                FakeStoreProductDto.class,
                id
        );
        if (responseEntity.hasBody()
                && responseEntity.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
            return mapToProduct(responseEntity.getBody());
        }

        return null;
    }

    @Override
    public Product replaceProduct(Long id, Product product) {
        if (getProductDetailsById(product.getId()) == null) {
            throw new FakeStoreApiProductException("Product with id " + id + " doesn't exists", FakeStoreApiExceptionType.PRODUCT_NOT_FOUND);
        }
        FakeStoreProductDto fakeStoreProductDto = mapToProductDto(product);
        ResponseEntity<FakeStoreProductDto> responseEntity = putForEntity(
                "/products/{id}",
                fakeStoreProductDto,
                FakeStoreProductDto.class,
                id
        );
        if (
                responseEntity.hasBody() &&
                        responseEntity.getStatusCode().equals(HttpStatusCode.valueOf(200))
        ) {
            return mapToProduct(responseEntity.getBody());
        }

        return null;
    }

    @Override
    public Product createProduct(Product product) {
        if (getProductDetailsById(product.getId()) != null) {
            String exceptionMessage = String.format(
                    "Product with id %d already exists. Cannot add a new product: %s",
                    product.getId(), product.getTitle());
            throw new FakeStoreApiProductException(exceptionMessage, FakeStoreApiExceptionType.PRODUCT_ALREADY_EXISTS);
        }
        FakeStoreProductDto fakeStoreProductDto = mapToProductDto(product);
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<FakeStoreProductDto> responseEntity = restTemplate.postForEntity(
                "/products",
                fakeStoreProductDto,
                FakeStoreProductDto.class
        );
        if (responseEntity.hasBody() && responseEntity.getStatusCode().is2xxSuccessful()) {
            return mapToProduct(responseEntity.getBody());
        }
        return null;
    }

    public <T> ResponseEntity<T> putForEntity(String url, @Nullable Object request,
                                              Class<T> responseType, Object... uriVariables) throws RestClientException {
        RestTemplate restTemplate = getRestTemplate();
        RequestCallback requestCallback = restTemplate.httpEntityCallback(request, responseType);
        ResponseExtractor<ResponseEntity<T>> responseExtractor = restTemplate.responseEntityExtractor(responseType);
        return restTemplate.execute(url, HttpMethod.PUT, requestCallback, responseExtractor, uriVariables);
    }

    private Product mapToProduct(FakeStoreProductDto fakeStoreProductDto) {
        Category category = new Category();
        category.setName(fakeStoreProductDto.getCategory());

        Product product = new Product();
        product.setId(fakeStoreProductDto.getId());
        product.setTitle(fakeStoreProductDto.getTitle());
        product.setDescription(fakeStoreProductDto.getDescription());
        product.setCategory(category);
        product.setPrice(fakeStoreProductDto.getPrice());
        return product;
    }

    private FakeStoreProductDto mapToProductDto(Product product) {
        FakeStoreProductDto fakeStoreProductDto = new FakeStoreProductDto();
        fakeStoreProductDto.setId(product.getId());
        fakeStoreProductDto.setTitle(product.getTitle());
        fakeStoreProductDto.setPrice(product.getPrice());
        fakeStoreProductDto.setDescription(product.getDescription());
        fakeStoreProductDto.setImage(product.getImageUrl());
        if (product.getCategory() != null) {
            fakeStoreProductDto.setCategory(product.getCategory().getName());
        }
        return fakeStoreProductDto;
    }
}
