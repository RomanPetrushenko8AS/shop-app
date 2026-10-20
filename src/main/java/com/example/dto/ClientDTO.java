package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;


public class ClientDTO {
    @Schema(description ="ID клиента" , example = "2", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;
    @Schema(description ="Имя клиента" , example ="Олег" ,required = true)
    private String name;
    @Schema(description = "Фамилия клиента", example = "Вераков", required = true)
    private String surname;
    @Schema(description = "Почта клиента", example = "OlegVerak@mail.ru", required = true)
    private String email;
    @Schema(description = "номер телефона", example = "79874563210", required = true)
    private String phone;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
