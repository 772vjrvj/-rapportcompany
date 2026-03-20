package com.rapportcompany.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class BiznoService {

    private static final String BASE_URL = "https://bizno.net";
    private static final int TIMEOUT_MILLIS = 30000;

    @Value("${bizno.user-agent}")
    private String userAgent;

    @Value("${bizno.detail-delay-millis:700}")
    private long detailDelayMillis;

    public Map<String, Object> searchAndDetail(String companyName, String ownerName) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("error", 0);
        result.put("success", false);
        result.put("companyName", companyName);
        result.put("ownerName", ownerName);

        try {
            Connection session = createSession();

            Map<String, Object> searchResult = doSearch(session, companyName, ownerName);
            result.put("search", searchResult);

            if (!Boolean.TRUE.equals(searchResult.get("success"))) {
                result.put("error", searchResult.getOrDefault("error", 1));
                result.put("message", searchResult.getOrDefault("message", "search 실패"));
                return result;
            }

            String article = String.valueOf(searchResult.getOrDefault("article", ""));
            String searchUrl = String.valueOf(searchResult.getOrDefault("url", BASE_URL + "/"));

            if (article.isBlank()) {
                result.put("error", 1);
                result.put("message", "article 값이 없습니다.");
                return result;
            }

            sleepQuietly(detailDelayMillis);

            Map<String, Object> detailResult = doDetail(session, article, searchUrl);
            result.put("detail", detailResult);

            if (!Boolean.TRUE.equals(detailResult.get("success"))) {
                result.put("error", detailResult.getOrDefault("error", 1));
                result.put("message", detailResult.getOrDefault("message", "detail 실패"));
                return result;
            }

            result.put("success", true);
            result.put("article", article);
            result.put("data", detailResult.get("data"));
            result.put("message", "");
            return result;

        } catch (Exception e) {
            log.error("[BiznoService.searchAndDetail] error", e);
            result.put("error", 1);
            result.put("success", false);
            result.put("message", (e != null && e.getMessage() != null) ? e.getMessage() : String.valueOf(e));
            return result;
        }
    }

    public Map<String, Object> search(String companyName, String ownerName) {
        try {
            return doSearch(createSession(), companyName, ownerName);
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("error", 1);
            result.put("success", false);
            result.put("companyName", companyName);
            result.put("ownerName", ownerName);
            result.put("message", (e != null && e.getMessage() != null) ? e.getMessage() : String.valueOf(e));
            return result;
        }
    }

    public Map<String, Object> detail(String article) {
        try {
            return doDetail(createSession(), article, BASE_URL + "/");
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("error", 1);
            result.put("success", false);
            result.put("article", article);
            result.put("message", (e != null && e.getMessage() != null) ? e.getMessage() : String.valueOf(e));
            return result;
        }
    }

    private Connection createSession() {
        return Jsoup.newSession()
                .userAgent(userAgent)
                .timeout(TIMEOUT_MILLIS)
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("accept-language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .referrer(BASE_URL + "/");
    }

    private Map<String, Object> doSearch(Connection session, String companyName, String ownerName) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();

        String normalizedCompanyName = normalizeSearchCompanyName(companyName);
        String url = BASE_URL + "/?area=&query=" + URLEncoder.encode(normalizedCompanyName, StandardCharsets.UTF_8);

        result.put("error", 0);
        result.put("success", false);
        result.put("companyName", companyName);
        result.put("normalizedCompanyName", normalizedCompanyName);
        result.put("ownerName", ownerName);
        result.put("url", url);

        Document doc = session.newRequest(url).get();
        String html = doc.outerHtml();

        if (isBlockedHtml(html)) {
            result.put("error", 1);
            result.put("message", "bizno 검색 차단/제한 페이지 감지");
            return result;
        }

        Elements details = doc.select(".details");
        int detailsCount = details.size();

        for (Element d : details) {
            String h5 = safeText(d.selectFirst("h5"));
            if (!isOwnerMatch(ownerName, h5)) {
                continue;
            }

            Element aTag = d.selectFirst("a[href^=/article/]");
            if (aTag == null) {
                continue;
            }

            String href = safeAttr(aTag, "href");
            if (href.isBlank()) {
                continue;
            }

            String article = href.replace("/article/", "").trim();
            String company = safeText(d.selectFirst("h4"));

            result.put("success", true);
            result.put("article", article);
            result.put("회사명", company);
            result.put("detailsCount", detailsCount);
            result.put("message", "");
            return result;
        }

        result.put("detailsCount", detailsCount);
        result.put("article", "");
        result.put("message", "검색 결과에서 일치 항목을 찾지 못했습니다.");
        return result;
    }

    private Map<String, Object> doDetail(Connection session, String article, String referrerUrl) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        String url = BASE_URL + "/article/" + article;

        result.put("error", 0);
        result.put("success", false);
        result.put("article", article);
        result.put("url", url);

        Document doc = session.newRequest(url)
                .referrer(referrerUrl)
                .get();

        String html = doc.outerHtml();

        if (isBlockedHtml(html)) {
            result.put("error", 1);
            result.put("message", "bizno 상세 차단/제한 페이지 감지");
            return result;
        }

        Element table = doc.selectFirst("table.table_guide01");
        if (table == null) {
            result.put("message", "상세 테이블(table.table_guide01)을 찾지 못했습니다.");
            return result;
        }

        Map<String, String> data = new LinkedHashMap<>();
        int rowCount = 0;

        for (Element tr : table.select("tr")) {
            String key = safeText(tr.selectFirst("th"));
            String val = safeText(tr.selectFirst("td"));

            if (!key.isBlank()) {
                data.put(key, val);
                rowCount++;
            }
        }

        result.put("success", true);
        result.put("rowCount", rowCount);
        result.put("data", data);
        result.put("message", "");
        return result;
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

    private String safeAttr(Element el, String attr) {
        return el == null ? "" : el.attr(attr).trim();
    }

    private boolean isOwnerMatch(String inputOwner, String scrapedOwner) {
        String in = normalizeOwner(inputOwner);
        String sc = normalizeOwner(scrapedOwner);
        if (in.isBlank() || sc.isBlank()) {
            return false;
        }
        return in.equals(sc) || in.contains(sc) || sc.contains(in);
    }

    private String normalizeOwner(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("*", "").replace(" ", "").trim();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}