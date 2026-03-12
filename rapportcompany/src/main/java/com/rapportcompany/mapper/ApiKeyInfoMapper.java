// package com.mproject.app.mapper;

package com.rapportcompany.mapper;

import com.rapportcompany.dto.ApiKeyInfoDto;
import com.rapportcompany.entity.ApiKeyInfoEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyInfoMapper {

    // DTO -> Entity
    public ApiKeyInfoEntity toEntity(ApiKeyInfoDto dto) {
        if (dto == null) {
            return null;
        }

        ApiKeyInfoEntity entity = new ApiKeyInfoEntity();
        entity.setServerId(dto.getServerId());
        entity.setServerUrl(dto.getServerUrl());
        entity.setServerApiKey(dto.getServerApiKey());
        entity.setUseYn(dto.getUseYn());
        entity.setCreateDt(dto.getCreateDt());

        return entity;
    }

    // Entity -> DTO
    public ApiKeyInfoDto toDto(ApiKeyInfoEntity entity) {
        if (entity == null) {
            return null;
        }

        ApiKeyInfoDto dto = new ApiKeyInfoDto();
        dto.setServerId(entity.getServerId());
        dto.setServerUrl(entity.getServerUrl());
        dto.setServerApiKey(entity.getServerApiKey());
        dto.setUseYn(entity.getUseYn());
        dto.setCreateDt(entity.getCreateDt());

        return dto;
    }
}