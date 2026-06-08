package com.hackathon.wishlist.config;

import com.hackathon.wishlist.service.FileStorageService;
import java.nio.file.Paths;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 업로드된 이미지(/uploads/**)를 파일 시스템에서 서빙 */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String dir = Paths.get(FileStorageService.UPLOAD_DIR)
                .toAbsolutePath().normalize().toString().replace('\\', '/');
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + dir + "/");
    }

    /**
     * 멀티파트 폼(파일 업로드)의 _csrf 필드를 Spring Security CSRF 필터가 읽을 수 있도록,
     * 보안 필터 체인보다 먼저 멀티파트를 파싱한다.
     * 단, 문자인코딩 필터(HIGHEST_PRECEDENCE, UTF-8)보다는 뒤에 와야 한글 폼필드가 깨지지 않으므로
     * order 를 그보다 살짝 뒤(+10)로 둔다. (보안 필터 체인 -100 보다는 여전히 앞)
     */
    @Bean
    public FilterRegistrationBean<MultipartFilter> multipartFilterRegistration() {
        FilterRegistrationBean<MultipartFilter> reg =
                new FilterRegistrationBean<>(new MultipartFilter());
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return reg;
    }
}
