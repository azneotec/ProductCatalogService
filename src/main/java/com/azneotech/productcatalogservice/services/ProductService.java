package com.azneotech.productcatalogservice.services;

import com.azneotech.productcatalogservice.dtos.FakeStoreProductDto;
import com.azneotech.productcatalogservice.models.Category;
import com.azneotech.productcatalogservice.models.Product;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductService implements IProductService {

    private RestTemplate restTemplate;
    private RestTemplateBuilder restTemplateBuilder;

    public  ProductService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = restTemplateBuilder.build();
        }
        return restTemplate;
    }

//    public ProductService() {
//        this.restTemplate = new RestTemplateBuilder()
//                .rootUri("https://fakestoreapi.com")
//                .build();
//    }

    @Override
    public Product getProductDetailsById(Long id) {
        RestTemplate restTemplate = getRestTemplate();
        FakeStoreProductDto fakeStoreProductDto = restTemplate.getForObject("https://fakestoreapi.com/products/" + id, FakeStoreProductDto.class);
        assert fakeStoreProductDto != null;

        Category category = new Category();
        category.setName(fakeStoreProductDto.getCategory());

        Product product = new Product();
        product.setId(fakeStoreProductDto.getId());
        product.setTitle(fakeStoreProductDto.getTitle());
        product.setDescription(fakeStoreProductDto.getDescription());
        product.setCategory(category);

        return product;
    }

}
