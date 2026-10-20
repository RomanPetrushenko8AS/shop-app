package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDTO {
    @Schema(description = "ID заказа", example = "12", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;
    @Schema(description = "ID клиента", example = "5", required = true)
    private Integer clientId;
    @Schema(description = "Id товара", example = "9", required = true)
    private Integer productId;
    @Schema(description = "Количество заказов", example = "90", required = true)
    private Integer quantity;
    @Schema(description = "Полная стоимость заказа", example = "12000")
    private BigDecimal totalPrice;
    @Schema(description = "Статус товара", example = "NEW")
    private String status;
    @Schema(description = "Дата создания заказа", example = "12.01.2025", required = true)
    private LocalDateTime createdAt;

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
