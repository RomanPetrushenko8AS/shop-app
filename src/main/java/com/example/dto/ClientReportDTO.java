package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class ClientReportDTO {
    @Schema(description = "ID клиента", example = "3", required = true)
    private Integer clientId;
    @Schema(description = "Имя и фамилия клиента", example = "Петр Иванов", required = true)
    private String fullName;
    @Schema(description = "Количество заказов клиента", example = "45", required = true)
    private Long ordersCount;
    @Schema(description = "Стоимость всех заказов", example = "2345.00")
    private BigDecimal totalSpent;

    public ClientReportDTO(Integer clientId, String fullName, Long ordersCount, BigDecimal totalSpent) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.ordersCount = ordersCount;
        this.totalSpent = totalSpent;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(Long ordersCount) {
        this.ordersCount = ordersCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }
}
