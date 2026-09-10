package org.example.bai1.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bai1.dto.ProductInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClientRT {

    private final RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE_URL = "http://product-service/api/products/{id}";

    public ProductInfo getById(Long productId) {
        try {
            log.info("Fetching product info for productId: {} via ProductServiceClientRT", productId);
            return restTemplate.getForObject(PRODUCT_SERVICE_URL, ProductInfo.class, productId);
        } catch (ResourceAccessException ex) {
            // LOI TIMEOUT / NETWORK: product-service bị treo hoặc quá tải
            log.error("Timeout or Network Error when calling product-service for productId {}: {}", productId, ex.getMessage());
            return getTimeoutFallback(productId);
        } catch (HttpClientErrorException.NotFound ex) {
            // LOI 404 NOT FOUND: Sản phẩm không tồn tại trong DB của product-service
            log.warn("Product with id {} not found (404) in product-service: {}", productId, ex.getMessage());
            return getNotFoundFallback(productId);
        } catch (Exception ex) {
            // LOI CHUNG KHAC
            log.error("Unexpected error calling product-service for productId {}: {}", productId, ex.getMessage());
            return getGenericFallback(productId);
        }
    }

    private ProductInfo getTimeoutFallback(Long productId) {
        return ProductInfo.builder()
                .id(productId)
                .name("Default Product (Timeout Fallback)")
                .price(0.0)
                .description("Service is temporarily unreachable due to timeout")
                .status("TIMEOUT_FALLBACK")
                .build();
    }

    private ProductInfo getNotFoundFallback(Long productId) {
        return ProductInfo.builder()
                .id(productId)
                .name("Unknown Product")
                .price(0.0)
                .description("Product not found")
                .status("NOT_FOUND")
                .build();
    }

    private ProductInfo getGenericFallback(Long productId) {
        return ProductInfo.builder()
                .id(productId)
                .name("System Error Fallback Product")
                .price(0.0)
                .description("Service error occurred")
                .status("ERROR_FALLBACK")
                .build();
    }
}
