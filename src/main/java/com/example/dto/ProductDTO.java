package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO товара")
public class ProductDTO {
    @Schema(description = "ID товара", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;
    @Schema(description = "Название товара", example = "Беспроводная мышь", required = true)
    private String name;
    @Schema(description = "Описание товара", example = "USB-адаптер")
    private String description;
    @Schema(description = "Цена товара в рублях", example = "1290.50", required = true)
    private BigDecimal price;
    @Schema(description = "Количество на складе", example = "20", required = true)
    private Integer quantity;


    public String getDescription() {
        return description;
    }

    public void setDescription(String decription) {
        this.description = decription;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
