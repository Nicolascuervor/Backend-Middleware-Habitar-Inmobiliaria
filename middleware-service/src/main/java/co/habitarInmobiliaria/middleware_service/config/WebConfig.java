package co.habitarinmobiliaria.middleware_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<Filter> iframeHeaderFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter((req, res, chain) -> {
            jakarta.servlet.http.HttpServletResponse httpRes =
                    (jakarta.servlet.http.HttpServletResponse) res;
            httpRes.setHeader("X-Frame-Options", "ALLOWALL");
            httpRes.setHeader("Content-Security-Policy", "frame-ancestors *");
            httpRes.setHeader("ngrok-skip-browser-warning", "true");
            chain.doFilter(req, res);
        });
        bean.addUrlPatterns("/*");
        return bean;
    }
}