package com.rapportcompany.service;

import com.rapportcompany.dto.ApiKeyInfoDto;
import com.rapportcompany.entity.ApiKeyInfoEntity;
import com.rapportcompany.mapper.ApiKeyInfoMapper;
import com.rapportcompany.repository.ApiKeyInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyInfoService {

    private final ApiKeyInfoRepository apiKeyInfoRepository;
    private final ApiKeyInfoMapper apiKeyInfoMapper;
    private final BiznoService biznoService;

    public Map<String, Object> search(
            String requestApiKey,
            String userId,
            String companyName,
            String ownerName,
            String requestIp
    ) {
        ApiKeyInfoEntity apiKeyInfo = getActiveApiKeyInfo();

        Map<String, Object> authResult = validateApiKey(apiKeyInfo, requestApiKey, userId, requestIp, "search");
        if (authResult != null) {
            return authResult;
        }

        log.info("[ApiKeyInfoService.search] API_KEY 검증 성공. serverId={}, ip={}, userId={}, companyName={}, ownerName={}",
                apiKeyInfo.getServerId(), requestIp, safe(userId), companyName, ownerName);

        return biznoService.search(companyName, ownerName);
    }

    public Map<String, Object> detail(
            String requestApiKey,
            String userId,
            String article,
            String requestIp
    ) {
        ApiKeyInfoEntity apiKeyInfo = getActiveApiKeyInfo();

        Map<String, Object> authResult = validateApiKey(apiKeyInfo, requestApiKey, userId, requestIp, "detail");
        if (authResult != null) {
            return authResult;
        }

        log.info("[ApiKeyInfoService.detail] API_KEY 검증 성공. serverId={}, ip={}, userId={}, article={}",
                apiKeyInfo.getServerId(), requestIp, safe(userId), article);

        return biznoService.detail(article);
    }

    public Map<String, Object> searchAndDetail(
            String requestApiKey,
            String userId,
            String companyName,
            String ownerName,
            String requestIp
    ) {
        ApiKeyInfoEntity apiKeyInfo = getActiveApiKeyInfo();

        Map<String, Object> authResult = validateApiKey(apiKeyInfo, requestApiKey, userId, requestIp, "searchAndDetail");
        if (authResult != null) {
            return authResult;
        }

        log.info("[ApiKeyInfoService.searchAndDetail] API_KEY 검증 성공. serverId={}, ip={}, userId={}, companyName={}, ownerName={}",
                apiKeyInfo.getServerId(), requestIp, safe(userId), companyName, ownerName);

        return biznoService.searchAndDetail(companyName, ownerName);
    }

    public Map<String, Object> saveApiKeyInfo(ApiKeyInfoDto apiKeyInfoDto) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (apiKeyInfoDto == null) {
            result.put("error", 1);
            result.put("success", false);
            result.put("message", "apiKeyInfo가 없습니다.");
            return result;
        }

        if (safe(apiKeyInfoDto.getServerId()).trim().isEmpty()) {
            result.put("error", 1);
            result.put("success", false);
            result.put("message", "serverId가 없습니다.");
            return result;
        }

        if (safe(apiKeyInfoDto.getServerUrl()).trim().isEmpty()) {
            result.put("error", 1);
            result.put("success", false);
            result.put("message", "serverUrl이 없습니다.");
            return result;
        }

        if (safe(apiKeyInfoDto.getServerApiKey()).trim().isEmpty()) {
            result.put("error", 1);
            result.put("success", false);
            result.put("message", "serverApiKey가 없습니다.");
            return result;
        }

        if (safe(apiKeyInfoDto.getUseYn()).trim().isEmpty()) {
            apiKeyInfoDto.setUseYn("Y");
        }

        if (safe(apiKeyInfoDto.getCreateDt()).trim().isEmpty()) {
            apiKeyInfoDto.setCreateDt(currentDateTime());
        }

        ApiKeyInfoEntity entity = apiKeyInfoMapper.toEntity(apiKeyInfoDto);

        boolean exists = apiKeyInfoRepository.existsById(entity.getServerId());
        apiKeyInfoRepository.save(entity);

        if (exists) {
            log.info("[ApiKeyInfoService.saveApiKeyInfo] update success. serverId={}", entity.getServerId());
        } else {
            log.info("[ApiKeyInfoService.saveApiKeyInfo] insert success. serverId={}", entity.getServerId());
        }

        result.put("error", 0);
        result.put("success", true);
        result.put("message", "저장 완료");
        result.put("serverId", entity.getServerId());
        return result;
    }

    private ApiKeyInfoEntity getActiveApiKeyInfo() {
        Optional<ApiKeyInfoEntity> optional = apiKeyInfoRepository.findFirstByUseYnOrderByCreateDtDesc("Y");
        return optional.orElse(null);
    }

    private Map<String, Object> validateApiKey(
            ApiKeyInfoEntity apiKeyInfo,
            String requestApiKey,
            String userId,
            String requestIp,
            String apiName
    ) {
        if (apiKeyInfo == null) {
            log.warn("[ApiKeyInfoService.{}] API_KEY_INFO 정보가 없습니다. ip={}, userId={}",
                    apiName, requestIp, safe(userId));
            return failResult("API_KEY_INFO 정보가 없습니다.");
        }

        if (!"Y".equalsIgnoreCase(safe(apiKeyInfo.getUseYn()))) {
            log.warn("[ApiKeyInfoService.{}] USE_YN이 N 입니다. serverId={}, ip={}, userId={}",
                    apiName, apiKeyInfo.getServerId(), requestIp, safe(userId));
            return failResult("사용 중지된 API_KEY 입니다.");
        }

        String dbApiKey = safe(apiKeyInfo.getServerApiKey()).trim();
        String inputApiKey = safe(requestApiKey).trim();

        if (inputApiKey.isEmpty()) {
            log.warn("[ApiKeyInfoService.{}] 요청 API_KEY가 비어있습니다. serverId={}, ip={}, userId={}",
                    apiName, apiKeyInfo.getServerId(), requestIp, safe(userId));
            return failResult("API_KEY가 없습니다.");
        }

        if (dbApiKey.isEmpty()) {
            log.warn("[ApiKeyInfoService.{}] DB API_KEY가 비어있습니다. serverId={}, ip={}, userId={}",
                    apiName, apiKeyInfo.getServerId(), requestIp, safe(userId));
            return failResult("서버 API_KEY 설정이 비어있습니다.");
        }

        if (!dbApiKey.equals(inputApiKey)) {
            log.warn("[ApiKeyInfoService.{}] API_KEY 불일치. serverId={}, ip={}, userId={}",
                    apiName, apiKeyInfo.getServerId(), requestIp, safe(userId));
            return failResult("API_KEY가 일치하지 않습니다.");
        }

        return null;
    }

    private Map<String, Object> failResult(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("error", 1);
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String currentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d-%02d-%02d %02d:%02d:%02d",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond());
    }
}