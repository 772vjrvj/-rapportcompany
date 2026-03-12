package com.rapportcompany.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class BiznoService {

    private static final String BASE_URL = "https://bizno.net";

    public Map<String, Object> search(String companyName, String ownerName) {
        Map<String, Object> result = new LinkedHashMap<>();

        String normalizedCompanyName = normalizeSearchCompanyName(companyName);

        result.put("error", 0);
        result.put("success", false);
        result.put("companyName", companyName);
        result.put("normalizedCompanyName", normalizedCompanyName);
        result.put("ownerName", ownerName);

        try {
            String url = BASE_URL + "/?area=&query=" + URLEncoder.encode(normalizedCompanyName, StandardCharsets.UTF_8);

            Document doc = Jsoup.connect(url)
                    .userAgent(getUserAgent())
                    .header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("cache-control", "no-cache")
                    .header("pragma", "no-cache")
                    .referrer(BASE_URL + "/")
                    .timeout(30000)
                    .get();

            String html = doc.outerHtml();
            if (isBlockedHtml(html)) {
                result.put("error", 1);
                result.put("success", false);
                result.put("message", "bizno 검색 차단/제한 페이지 감지");
                result.put("url", url);
                return result;
            }

            Elements details = doc.select(".details");

            int hit = 0;
            for (Element d : details) {
                hit++;

                String h5 = safeText(d.selectFirst("h5"));
                if (!safeEqualsTrim(h5, ownerName)) {
                    continue;
                }

                Element aTag = d.selectFirst("a[href^=/article/]");
                if (aTag == null) {
                    continue;
                }

                String href = aTag.attr("href");
                if (href == null || href.isBlank()) {
                    continue;
                }

                String article = href.replace("/article/", "").trim();
                String company = safeText(d.selectFirst("h4"));

                result.put("success", true);
                result.put("article", article);
                result.put("회사명", company);
                result.put("detailsCount", hit);
                result.put("url", url);
                result.put("message", "");
                return result;
            }

            result.put("detailsCount", hit);
            result.put("article", "");
            result.put("message", "검색 결과에서 일치 항목을 찾지 못했습니다.");
            result.put("url", url);
            return result;

        } catch (Exception e) {
            log.error("[BiznoService.search] error", e);
            result.put("error", 1);
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    public Map<String, Object> detail(String article) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("error", 0);
        result.put("success", false);
        result.put("article", article);

        try {
            String url = BASE_URL + "/article/" + article;

            Document doc = Jsoup.connect(url)
                    .userAgent(getUserAgent())
                    .header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("cache-control", "no-cache")
                    .header("pragma", "no-cache")
                    .referrer(BASE_URL + "/")
                    .timeout(30000)
                    .get();

            String html = doc.outerHtml();
            result.put("url", url);

            if (isBlockedHtml(html)) {
                result.put("error", 1);
                result.put("success", false);
                result.put("message", "bizno 상세 차단/제한 페이지 감지");
                return result;
            }

            Element table = doc.selectFirst("table.table_guide01");

            if (table == null) {
                result.put("message", "상세 테이블(table.table_guide01)을 찾지 못했습니다.");
                return result;
            }

            Map<String, String> data = new LinkedHashMap<>();
            int rowCnt = 0;

            for (Element tr : table.select("tr")) {
                Element th = tr.selectFirst("th");
                Element td = tr.selectFirst("td");

                String key = safeText(th);
                String val = safeTextWithNewLine(td);

                if (!key.isBlank()) {
                    data.put(key, val);
                    rowCnt++;
                }
            }

            result.put("success", true);
            result.put("rowCount", rowCnt);
            result.put("data", data);
            result.put("message", "");
            return result;

        } catch (Exception e) {
            log.error("[BiznoService.detail] error", e);
            result.put("error", 1);
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    private boolean isBlockedHtml(String html) {
        if (html == null || html.isBlank()) {
            return true;
        }

        String low = html.toLowerCase();

        String[] keywords = {
                "접근이 차단",
                "비정상적인 접근",
                "잠시 후 다시",
                "too many requests",
                "request blocked",
                "access denied",
                "forbidden",
                "현재 접속인원이 많아 접속이 지연되고 있습니다",
                "접속대기중",
                "접속 대기중",
                "stand-by state",
                "please try again. (1)"
        };

        for (String keyword : keywords) {
            if (low.contains(keyword.toLowerCase())) {
                log.warn("[BiznoService] blocked keyword detected: {}", keyword);
                return true;
            }
        }

        if (html.length() < 1200) {
            log.warn("[BiznoService] blocked suspicious html length: {}", html.length());
            return true;
        }

        return false;
    }

    private String normalizeSearchCompanyName(String name) {
        if (name == null) {
            return "";
        }

        String value = name.trim();
        value = value.replace("(주)", "");
        value = value.replace("주식회사", "");
        return value.trim();
    }

    private String safeText(Element el) {
        return el == null ? "" : el.text().trim();
    }

    private String safeTextWithNewLine(Element el) {
        if (el == null) {
            return "";
        }
        return el.text().trim();
    }

    private boolean safeEqualsTrim(String a, String b) {
        String aa = a == null ? "" : a.trim();
        String bb = b == null ? "" : b.trim();
        return aa.equals(bb);
    }

    private String getUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36";
    }
}