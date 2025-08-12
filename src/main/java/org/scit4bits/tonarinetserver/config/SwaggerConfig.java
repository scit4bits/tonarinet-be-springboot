package org.scit4bits.tonarinetserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@OpenAPIDefinition(
    servers = {
        @Server(url = "http://localhost:8999", description="local server"),
        @Server(url = "https://tnsv.thxx.xyz", description="live server")
    }
)
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }
 
    private Info apiInfo() {
        return new Info()
                .title("Tonarinet REST API Server")
                .description("Swagger Web UI For Tonarinet API Server")
                .version("1.0.0");
    }
}