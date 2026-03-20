package com.rapportcompany.restcontroller;

import com.rapportcompany.service.ApiKeyInfoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/bizno", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BiznoController {

    private final ApiKeyInfoService apiKeyInfoService;

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestHeader(name = "X-API-KEY") String apiKey,
            @RequestParam(name = "userId", required = false, defaultValue = "") String userId,
            @RequestParam(name = "companyName") String companyName,
            @RequestParam(name = "ownerName", required = false, defaultValue = "") String ownerName,
            HttpServletRequest request
    ) {
        String requestIp = getRequestIp(request);
        return apiKeyInfoService.search(apiKey, userId, companyName, ownerName, requestIp);
    }

    @GetMapping("/detail")
    public Map<String, Object> detail(
            @RequestHeader(name = "X-API-KEY") String apiKey,
            @RequestParam(name = "userId", required = false, defaultValue = "") String userId,
            @RequestParam(name = "article") String article,
            HttpServletRequest request
    ) {
        String requestIp = getRequestIp(request);
        return apiKeyInfoService.detail(apiKey, userId, article, requestIp);
    }

    @GetMapping("/search-and-detail")
    public Map<String, Object> searchAndDetail(
            @RequestHeader(name = "X-API-KEY") String apiKey,
            @RequestParam(name = "userId", required = false, defaultValue = "") String userId,
            @RequestParam(name = "companyName") String companyName,
            @RequestParam(name = "ownerName", required = false, defaultValue = "") String ownerName,
            HttpServletRequest request
    ) {
        String requestIp = getRequestIp(request);
        return apiKeyInfoService.searchAndDetail(apiKey, userId, companyName, ownerName, requestIp);
    }

    private String getRequestIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }
}