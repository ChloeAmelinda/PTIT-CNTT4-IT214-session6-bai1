package org.example.bai1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInfo {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String status;
}
