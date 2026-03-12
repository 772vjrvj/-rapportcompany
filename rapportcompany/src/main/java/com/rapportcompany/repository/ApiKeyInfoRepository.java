package com.rapportcompany.repository;

import com.rapportcompany.entity.ApiKeyInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyInfoRepository extends JpaRepository<ApiKeyInfoEntity, String> {

    Optional<ApiKeyInfoEntity> findFirstByUseYnOrderByCreateDtDesc(String useYn);
}