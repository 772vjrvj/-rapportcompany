package com.rapportcompany.restcontroller;

import com.rapportcompany.dto.ApiKeyInfoDto;
import com.rapportcompany.service.ApiKeyInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/internal/api-key-info", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiKeyInfoAdminController {

    private final ApiKeyInfoService apiKeyInfoService;

    @Value("${app.admin.update-key}")
    private String adminUpdateKey;

    @PostMapping("/save")
    public Map<String, Object> saveApiKeyInfo(
            @RequestHeader(name = "X-ADMIN-KEY", required = false) String requestAdminKey,
            @RequestBody ApiKeyInfoDto apiKeyInfoDto
    ) {
        if (requestAdminKey == null || requestAdminKey.trim().isEmpty()) {
            return failResult("X-ADMIN-KEY가 없습니다.");
        }

        if (!requestAdminKey.trim().equals(adminUpdateKey)) {
            return failResult("관리자 인증키가 일치하지 않습니다.");
        }

        return apiKeyInfoService.saveApiKeyInfo(apiKeyInfoDto);
    }

    private Map<String, Object> failResult(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("error", 1);
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}