package com.hackathon.wishlist.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 상품 URL 의 HTML 을 받아 대표 이미지(og:image)를 추출한다.
 * - 외부 네트워크 호출이므로 어떤 예외가 나든 null 을 반환(앱이 죽지 않게).
 * - User-Agent 를 브라우저처럼 위장, 타임아웃 5초.
 */
@Component
public class OgImageExtractor {

    private static final Logger log = LoggerFactory.getLogger(OgImageExtractor.class);

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private static final int TIMEOUT_MS = 5000;

    /**
     * 주어진 URL 페이지에서 og:image (없으면 twitter:image) 값을 반환.
     * 실패하거나 못 찾으면 null.
     */
    public String extractOgImage(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            Document doc = Jsoup.connect(url.trim())
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)   // 4xx/5xx 여도 파싱 시도
                    .get();

            String image = firstNonBlank(
                    doc.select("meta[property=og:image]").attr("content"),
                    doc.select("meta[name=og:image]").attr("content"),
                    doc.select("meta[property=twitter:image]").attr("content"),
                    doc.select("meta[name=twitter:image]").attr("content"));

            if (image == null) {
                log.info("og:image 없음 url={}", url);
                return null;
            }
            return image.trim();
        } catch (Exception e) {
            // 네트워크 오류/타임아웃/잘못된 URL 등 모든 예외를 흡수
            log.warn("og:image 추출 실패 url={} : {}", url, e.toString());
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
