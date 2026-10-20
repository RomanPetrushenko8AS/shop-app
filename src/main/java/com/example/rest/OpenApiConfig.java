package com.example.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(
        title = "shop REST API",
        version = "1.0.0",
        description = "REST API для управления товарами, клиентами и заказами"
))

public class OpenApiConfig {
}
