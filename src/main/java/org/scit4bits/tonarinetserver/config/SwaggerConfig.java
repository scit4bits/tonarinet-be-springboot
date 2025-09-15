package org.scit4bits.tonarinetserver.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger (OpenAPI) 설정을 구성하는 클래스
 */
@OpenAPIDefinition(
        servers = {
                @Server(url = "http://localhost:8999", description = "local server"),
                @Server(url = "https://tnsv.thxx.xyz", description = "live server")
        }
)
@Configuration
public class SwaggerConfig {

    /**
     * API 정보를 생성합니다.
     * @return API 정보 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("Tonarinet REST API Server")
                .description("Swagger Web UI For Tonarinet API Server")
                .version("1.0.0");
    }

    /**
     * OpenAPI 설정을 위한 Bean을 생성합니다.
     * JWT Bearer 토큰 인증을 위한 설정을 포함합니다.
     * @return OpenAPI 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        // "bearerAuth"라는 이름의 보안 스키마 추가
                        .addSecuritySchemes("bearerAuth",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // 모든 API에 "bearerAuth" 보안 요구사항 추가
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList("bearerAuth"))
                .info(apiInfo());
    }
}
