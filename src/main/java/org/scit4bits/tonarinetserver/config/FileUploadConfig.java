package org.scit4bits.tonarinetserver.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FileUploadConfig {

    @Value("${spring.servlet.multipart.location:c:/upload}")
    private String uploadPath;

    @Bean
    public CommandLineRunner createUploadDirectory() {
        return args -> {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (created) {
                    log.info("Created upload directory: {}", uploadPath);
                } else {
                    log.warn("Failed to create upload directory: {}", uploadPath);
                }
            } else {
                log.info("Upload directory exists: {}", uploadPath);
            }
        };
    }
}
