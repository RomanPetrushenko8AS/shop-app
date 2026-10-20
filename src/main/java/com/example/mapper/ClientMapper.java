package com.example.mapper;

import com.example.dto.ClientDTO;
import com.example.entity.Client;

public class ClientMapper {

    public static ClientDTO toDTO(Client entity){
        if(entity == null){return null;}

        ClientDTO dto = new ClientDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSurname(entity.getSurname());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());

        return dto;
    }

    public static Client toEntity(ClientDTO dto){
        if(dto == null){return null;}

        Client entity = new Client();
        entity.setName(dto.getName());
        entity.setSurname(dto.getSurname());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());

        return entity;
    }
}
