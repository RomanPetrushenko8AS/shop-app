package com.example.mapper;

import com.example.dto.ProductDTO;
import com.example.entity.Product;

public class ProductMapper {
    public static ProductDTO toDTO(Product entity){
        if(entity == null){return null;}

        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setQuantity(entity.getQuantity());
        dto.setDescription(entity.getDescription());

        return dto;
    }

    public static Product toEntity(ProductDTO dto) {
        if (dto == null) return null;

        Product entity = new Product();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setQuantity(dto.getQuantity());
        return entity;
    }
}
