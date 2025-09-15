package org.scit4bits.tonarinetserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 파일 업로드 설정을 구성하는 클래스
 */
@Configuration
@Slf4j
public class FileUploadConfig {

    @Value("${spring.servlet.multipart.location:c:/upload}")
    private String uploadPath;

    /**
     * 애플리케이션 시작 시 업로드 디렉토리를 생성하는 CommandLineRunner를 빈으로 등록합니다.
     * @return CommandLineRunner 객체
     */
    @Bean
    public CommandLineRunner createUploadDirectory() {
        return args -> {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (created) {
                    log.info("업로드 디렉토리 생성 완료: {}", uploadPath);
                } else {
                    log.warn("업로드 디렉토리 생성 실패: {}", uploadPath);
                }
            } else {
                log.info("업로드 디렉토리가 이미 존재합니다: {}", uploadPath);
            }
        };
    }
}
