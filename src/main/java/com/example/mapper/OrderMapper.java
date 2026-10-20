package com.example.mapper;

import com.example.dto.OrderDTO;
import com.example.entity.Client;
import com.example.entity.Order;
import com.example.entity.Product;

import java.time.LocalDateTime;

public class OrderMapper {

    public static OrderDTO toDTO(Order entity) {
        if (entity == null) return null;

        OrderDTO dto = new OrderDTO();
        dto.setId(entity.getId());
        dto.setQuantity(entity.getQuantity());
        dto.setTotalPrice(entity.getTotalPrice());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getClient() != null) {
            dto.setClientId(entity.getClient().getId());
        }
        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
        }

        return dto;
    }

    public static Order toEntity(OrderDTO dto, Client client, Product product) {
        if (dto == null) return null;

        Order entity = new Order();
        entity.setClient(client);
        entity.setProduct(product);
        entity.setQuantity(dto.getQuantity());
        entity.setTotalPrice(dto.getTotalPrice());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "New");
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }
}