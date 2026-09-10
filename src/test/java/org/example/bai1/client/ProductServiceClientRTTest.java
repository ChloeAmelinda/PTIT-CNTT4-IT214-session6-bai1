package org.example.bai1.client;

import org.example.bai1.dto.ProductInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientRTTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceClientRT productServiceClientRT;

    private static final String PRODUCT_SERVICE_URL = "http://product-service/api/products/{id}";

    @Test
    @DisplayName("Test 1: Gọi service thành công - Happy Path")
    void testGetById_Success() {
        // Arrange
        Long productId = 100L;
        ProductInfo expectedProduct = ProductInfo.builder()
                .id(productId)
                .name("Laptop Dell XPS")
                .price(1500.0)
                .description("High end laptop")
                .status("ACTIVE")
                .build();

        when(restTemplate.getForObject(eq(PRODUCT_SERVICE_URL), eq(ProductInfo.class), eq(productId)))
                .thenReturn(expectedProduct);

        // Act
        ProductInfo result = productServiceClientRT.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Laptop Dell XPS", result.getName());
        assertEquals(1500.0, result.getPrice());
        assertEquals("ACTIVE", result.getStatus());

        verify(restTemplate, times(1)).getForObject(eq(PRODUCT_SERVICE_URL), eq(ProductInfo.class), eq(productId));
    }

    @Test
    @DisplayName("Test 2: Mô phỏng Timeout (ResourceAccessException) - Fallback Path")
    void testGetById_Timeout_ReturnsFallback() {
        // Arrange
        Long productId = 101L;
        when(restTemplate.getForObject(eq(PRODUCT_SERVICE_URL), eq(ProductInfo.class), eq(productId)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        // Act
        ProductInfo result = productServiceClientRT.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("TIMEOUT_FALLBACK", result.getStatus());
        assertEquals("Default Product (Timeout Fallback)", result.getName());
        assertTrue(result.getDescription().contains("timeout"));

        verify(restTemplate, times(1)).getForObject(eq(PRODUCT_SERVICE_URL), eq(ProductInfo.class), eq(productId));
    }

    @Test
    @DisplayName("Test 3: Mô phỏng 404 Not Found (HttpClientErrorException.NotFound)")
    void testGetById_NotFound_ReturnsFallback() {
        // Arrange
        Long productId = 999L;
        when(restTemplate.getForObject(eq(PRODUCT_SERVICE_URL), eq(ProductInfo.class), eq(productId)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        // Act
        ProductInfo result = productServiceClientRT.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("NOT_FOUND", result.getStatus());
        assertEquals("Unknown Product", result.getName());

        verify(restTemplate, times(1)).getForObject(eq(PRODUCT_SERVICE_URL), eq(ProductInfo.class), eq(productId));
    }
}
