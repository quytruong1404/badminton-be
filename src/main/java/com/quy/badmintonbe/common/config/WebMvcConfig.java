package com.quy.badmintonbe.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/payments/vnpay-callback",
                        "/api/branches", "/api/branches/**",
                        "/api/time-slots", "/api/time-slots/**",
                        "/api/courts", "/api/courts/**",
                        "/api/pricing", "/api/pricing/**",
                        "/api/products", "/api/products/**",
                        "/api/vouchers", "/api/vouchers/**",
                        "/api/reviews", "/api/reviews/**",
                        "/api/system-configs", "/api/system-configs/**",
                        "/api/cancellation-policies", "/api/cancellation-policies/**",
                        "/api/bookings/occupied-slots",
                        "/api/public/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
